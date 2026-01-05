package starlight.application.aireport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.aireport.reportgrader.util.AiReportResponseParser;
import starlight.application.aireport.provided.AiReportUseCase;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.AiReportGradingPort;
import starlight.application.aireport.required.AiReportQueryPort;
import starlight.application.businessplan.provided.BusinessPlanUseCase;
import starlight.application.businessplan.provided.dto.BusinessPlanResult;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.businessplan.util.BusinessPlanContentExtractor;
import starlight.application.aireport.required.OcrProviderPort;
import starlight.application.infrastructure.provided.LlmGenerator;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.aireport.exception.AiReportErrorType;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiReportServiceImpl implements AiReportUseCase {

    private final BusinessPlanQueryPort businessPlanQuery;
    private final BusinessPlanUseCase businessPlanService;
    private final AiReportQueryPort aiReportQuery;
    private final AiReportGradingPort aiReportGrader;
    private final ObjectMapper objectMapper;
    private final OcrProviderPort ocrProvider;
    private final AiReportResponseParser responseParser;
    private final BusinessPlanContentExtractor contentExtractor;
    private final LlmGenerator llmGenerator;

    @Override
    public AiReportResult gradeBusinessPlan(Long planId, Long memberId) {

        BusinessPlan plan = businessPlanQuery.findByIdOrThrow(planId);
        checkBusinessPlanOwned(plan, memberId);
        checkBusinessPlanWritingCompleted(plan);

        AiReportResult gradingResult = aiReportGrader.gradeContent(contentExtractor.extractContent(plan));

        String rawJsonString = getRawJsonAiReportResponseFromGradingResult(gradingResult);

        AiReport aiReport = upsertAiReportWithRawJsonStr(rawJsonString, plan);

        return responseParser.toResponse(aiReportQuery.save(aiReport));
    }

    @Override
    public AiReportResult createAndGradePdfBusinessPlan(String title, String pdfUrl, Long memberId) {

        BusinessPlanResult.Result businessPlanResult = businessPlanService.createBusinessPlanWithPdf(
                title,
                pdfUrl,
                memberId
        );
        Long businessPlanId = businessPlanResult.businessPlanId();
        BusinessPlan plan = businessPlanQuery.findByIdOrThrow(businessPlanId);

        String pdfText = ocrProvider.ocrPdfTextByUrl(pdfUrl);

        // PDF의 경우 기존 한 번에 LLM에 돌리는 방식을 사용
        String llmResponse = llmGenerator.generateReport(pdfText);
        AiReportResult gradingResult = responseParser.parse(llmResponse);

        String rawJsonString = getRawJsonAiReportResponseFromGradingResult(gradingResult);

        AiReport aiReport = upsertAiReportWithRawJsonStr(rawJsonString, plan);

        return responseParser.toResponse(aiReportQuery.save(aiReport));
    }

    @Override
    @Transactional(readOnly = true)
    public AiReportResult getAiReport(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQuery.findByIdOrThrow(planId);
        checkBusinessPlanOwned(plan, memberId);

        AiReport aiReport = aiReportQuery.findByBusinessPlanId(planId)
                .orElseThrow(() -> new AiReportException(AiReportErrorType.AI_REPORT_NOT_FOUND));

        return responseParser.toResponse(aiReport);
    }

    private String getRawJsonAiReportResponseFromGradingResult(AiReportResult gradingResult) {
        JsonNode gradingJsonNode = responseParser.convertToJsonNode(gradingResult);
        String rawJsonString;
        try {
            rawJsonString = objectMapper.writeValueAsString(gradingJsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JsonNode to string", e);
        }
        return rawJsonString;
    }

    private AiReport upsertAiReportWithRawJsonStr(String rawJsonString, BusinessPlan plan) {
        Optional<AiReport> existingReport = aiReportQuery.findByBusinessPlanId(plan.getId());

        AiReport aiReport;
        if (existingReport.isPresent()) {
            aiReport = existingReport.get();
            aiReport.update(rawJsonString);
        } else {
            aiReport = AiReport.create(plan.getId(), rawJsonString);
        }
        plan.updateStatus(PlanStatus.AI_REVIEWED);
        businessPlanQuery.save(plan);

        return aiReport;
    }

    private void checkBusinessPlanOwned(BusinessPlan plan, Long memberId) {
        if (!plan.isOwnedBy(memberId)) {
            throw new AiReportException(AiReportErrorType.UNAUTHORIZED_ACCESS);
        }
    }

    private void checkBusinessPlanWritingCompleted(BusinessPlan plan) {
        if (!plan.areWritingCompleted()) {
            throw new AiReportException(AiReportErrorType.NOT_READY_FOR_AI_REPORT);
        }
    }
}
