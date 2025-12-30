package starlight.application.aireport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.ai.util.AiReportResponseParser;
import starlight.application.aireport.provided.AiReportService;
import starlight.application.aireport.provided.dto.AiReportResponse;
import starlight.application.aireport.required.AiReportGrader;
import starlight.application.aireport.required.AiReportQuery;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.provided.dto.BusinessPlanResponse;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.util.BusinessPlanContentExtractor;
import starlight.application.aireport.required.OcrProvider;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.aireport.exception.AiReportErrorType;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiReportServiceImpl implements AiReportService {

    private final BusinessPlanQuery businessPlanQuery;
    private final BusinessPlanService businessPlanService;
    private final AiReportQuery aiReportQuery;
    private final AiReportGrader aiReportGrader;
    private final ObjectMapper objectMapper;
    private final OcrProvider ocrProvider;
    private final AiReportResponseParser responseParser;
    private final BusinessPlanContentExtractor contentExtractor;

    @Override
    public AiReportResponse gradeBusinessPlan(Long planId, Long memberId) {

        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        checkBusinessPlanOwned(plan, memberId);
        checkBusinessPlanWritingCompleted(plan);

        AiReportResponse gradingResult = aiReportGrader.gradeContent(contentExtractor.extractContent(plan));

        String rawJsonString = getRawJsonAiReportResponseFromGradingResult(gradingResult);

        AiReport aiReport = upsertAiReportWithRawJsonStr(rawJsonString, plan);

        return responseParser.toResponse(aiReportQuery.save(aiReport));
    }

    @Override
    public AiReportResponse createAndGradePdfBusinessPlan(String title, String pdfUrl, Long memberId) {

        BusinessPlanResponse.Result businessPlanResult = businessPlanService.createBusinessPlanWithPdf(
                title,
                pdfUrl,
                memberId
        );
        Long businessPlanId = businessPlanResult.businessPlanId();
        BusinessPlan plan = businessPlanQuery.getOrThrow(businessPlanId);

        String pdfText = ocrProvider.ocrPdfTextByUrl(pdfUrl);

        AiReportResponse gradingResult = aiReportGrader.gradeContent(pdfText);

        String rawJsonString = getRawJsonAiReportResponseFromGradingResult(gradingResult);

        AiReport aiReport = upsertAiReportWithRawJsonStr(rawJsonString, plan);

        return responseParser.toResponse(aiReportQuery.save(aiReport));
    }

    @Override
    @Transactional(readOnly = true)
    public AiReportResponse getAiReport(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        checkBusinessPlanOwned(plan, memberId);

        AiReport aiReport = aiReportQuery.findByBusinessPlanId(planId)
                .orElseThrow(() -> new AiReportException(AiReportErrorType.AI_REPORT_NOT_FOUND));

        return responseParser.toResponse(aiReport);
    }

    private String getRawJsonAiReportResponseFromGradingResult(AiReportResponse gradingResult) {
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
