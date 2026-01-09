package starlight.application.aireport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.aireport.provided.AiReportUseCase;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.AiReportCommandPort;
import starlight.application.aireport.required.AiReportQueryPort;
import starlight.application.aireport.required.ReportGraderPort;
import starlight.application.aireport.util.AiReportResponseParser;
import starlight.application.businessplan.required.BusinessPlanCommandPort;
import starlight.application.aireport.required.BusinessPlanCreationPort;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.businessplan.util.BusinessPlanContentExtractor;
import starlight.application.aireport.required.OcrProviderPort;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.aireport.exception.AiReportErrorType;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.enumerate.SectionType;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiReportService implements AiReportUseCase {

    private final BusinessPlanCommandPort businessPlanCommandPort;
    private final BusinessPlanQueryPort businessPlanQueryPort;
    private final BusinessPlanCreationPort businessPlanCreationPort;
    private final AiReportQueryPort aiReportQueryPort;
    private final AiReportCommandPort aiReportCommandPort;
    private final ReportGraderPort reportGrader;
    private final ObjectMapper objectMapper;
    private final OcrProviderPort ocrProvider;
    private final AiReportResponseParser responseParser;
    private final BusinessPlanContentExtractor contentExtractor;

    @Override
    public AiReportResult gradeBusinessPlan(Long planId, Long memberId) {
        log.info("사업계획서 AI 채점 시작. planId: {}, memberId: {}", planId, memberId);

        BusinessPlan plan = businessPlanQueryPort.findByIdOrThrow(planId);
        checkBusinessPlanOwned(plan, memberId);
        checkBusinessPlanWritingCompleted(plan);

        // 섹션별 내용 추출
        Map<SectionType, String> sectionContents = contentExtractor.extractSectionContents(plan);
        log.debug("사업계획서 섹션별 내용 추출 완료. 섹션 수: {}", sectionContents.size());

        // 전체 내용도 추출 (Supervisor용)
        String fullContent = contentExtractor.extractContent(plan);
        if (fullContent == null || fullContent.trim().isEmpty()) {
            log.error("추출된 사업계획서 내용이 비어있습니다. planId: {}", planId);
            throw new AiReportException(AiReportErrorType.AI_GRADING_FAILED);
        }

        AiReportResult gradingResult = reportGrader.gradeWithSectionAgents(sectionContents, fullContent);

        // 채점 결과 검증
        if (isInvalidGradingResult(gradingResult)) {
            log.error("채점 결과가 유효하지 않습니다. 모든 점수가 0이고 빈 배열입니다. planId: {}", planId);
            throw new AiReportException(AiReportErrorType.AI_GRADING_FAILED);
        }

        log.info("채점 완료. 총점: {}, planId: {}", gradingResult.totalScore(), planId);

        String rawJsonString = getRawJsonAiReportResponseFromGradingResult(gradingResult);

        AiReport aiReport = upsertAiReportWithRawJsonStr(rawJsonString, plan);

        return responseParser.toResponse(aiReportCommandPort.save(aiReport));
    }

    @Override
    public AiReportResult createAndGradePdfBusinessPlan(String title, String pdfUrl, Long memberId) {
        log.info("PDF 사업계획서 생성 및 AI 채점 시작. title: {}, pdfUrl: {}, memberId: {}", title, pdfUrl, memberId);

        Long businessPlanId = businessPlanCreationPort.createBusinessPlanWithPdf(title, pdfUrl, memberId);
        BusinessPlan plan = businessPlanQueryPort.findByIdOrThrow(businessPlanId);

        log.debug("OCR 시작. pdfUrl: {}", pdfUrl);
        String pdfText = ocrProvider.ocrPdfTextByUrl(pdfUrl);
        log.debug("OCR 완료. 텍스트 길이: {}", pdfText != null ? pdfText.length() : 0);

        if (pdfText == null || pdfText.trim().isEmpty()) {
            log.error("OCR로 추출된 텍스트가 비어있습니다. pdfUrl: {}", pdfUrl);
            throw new AiReportException(AiReportErrorType.AI_GRADING_FAILED);
        }

        // PDF의 경우 기존 한 번에 LLM에 돌리는 방식을 사용
        AiReportResult gradingResult = reportGrader.gradeWithFullPrompt(pdfText);

        // 채점 결과 검증
        if (isInvalidGradingResult(gradingResult)) {
            log.error("채점 결과가 유효하지 않습니다. 모든 점수가 0이고 빈 배열입니다. businessPlanId: {}", businessPlanId);
            throw new AiReportException(AiReportErrorType.AI_GRADING_FAILED);
        }

        log.info("PDF 채점 완료. 총점: {}, businessPlanId: {}", gradingResult.totalScore(), businessPlanId);

        String rawJsonString = getRawJsonAiReportResponseFromGradingResult(gradingResult);

        AiReport aiReport = upsertAiReportWithRawJsonStr(rawJsonString, plan);

        return responseParser.toResponse(aiReportCommandPort.save(aiReport));
    }

    @Override
    @Transactional(readOnly = true)
    public AiReportResult getAiReport(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQueryPort.findByIdOrThrow(planId);
        checkBusinessPlanOwned(plan, memberId);

        AiReport aiReport = aiReportQueryPort.findByBusinessPlanId(planId)
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
        Optional<AiReport> existingReport = aiReportQueryPort.findByBusinessPlanId(plan.getId());

        AiReport aiReport;
        if (existingReport.isPresent()) {
            aiReport = existingReport.get();
            aiReport.update(rawJsonString);
        } else {
            aiReport = AiReport.create(plan.getId(), rawJsonString);
        }
        plan.updateStatus(PlanStatus.AI_REVIEWED);
        businessPlanCommandPort.save(plan);

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

    /**
     * 채점 결과가 유효한지 검증
     * 모든 점수가 0이고 빈 배열인 경우 유효하지 않음
     */
    private boolean isInvalidGradingResult(AiReportResult result) {
        boolean allScoresZero = (result.problemRecognitionScore() == null || result.problemRecognitionScore() == 0) &&
                (result.feasibilityScore() == null || result.feasibilityScore() == 0) &&
                (result.growthStrategyScore() == null || result.growthStrategyScore() == 0) &&
                (result.teamCompetenceScore() == null || result.teamCompetenceScore() == 0);

        boolean allArraysEmpty = (result.strengths() == null || result.strengths().isEmpty()) &&
                (result.weaknesses() == null || result.weaknesses().isEmpty()) &&
                (result.sectionScores() == null || result.sectionScores().isEmpty());

        return allScoresZero && allArraysEmpty;
    }
}
