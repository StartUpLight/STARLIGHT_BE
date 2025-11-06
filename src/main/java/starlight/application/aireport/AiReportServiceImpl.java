package starlight.application.aireport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.ai.util.AiReportResponseParser;
import starlight.application.aireport.dto.AiReportResponse;
import starlight.application.aireport.provided.AiReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import starlight.application.aireport.required.AiReportGrader;
import starlight.application.aireport.required.AiReportQuery;
import starlight.application.businessplan.required.BusinessPlanQuery;
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
    private final AiReportQuery aiReportQuery;
    private final AiReportGrader aiReportGrader;
    private final ObjectMapper objectMapper;
    private final AiReportResponseParser responseParser;

    @Override
    public AiReportResponse gradeBusinessPlan(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        // 권한 및 작성 완료 검증 (LLM 호출 전에 검증)
        Optional<AiReport> existingReport = getOwnedAiReport(plan, memberId);

        // LLM 채점
        AiReportResponse gradingResult = aiReportGrader.grade(plan);

        // AiReportResponse를 JsonNode로 변환하여 RawJson
        JsonNode gradingJsonNode = responseParser.convertToJsonNode(gradingResult);
        String rawJsonString;
        try {
            rawJsonString = objectMapper.writeValueAsString(gradingJsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JsonNode to string", e);
        }

        // AiReport 생성 또는 업데이트
        AiReport aiReport;

        if (existingReport.isPresent()) {
            aiReport = existingReport.get();
            aiReport.update(rawJsonString);
        } else {
            aiReport = AiReport.create(planId, rawJsonString);
            plan.updateStatus(PlanStatus.AI_REVIEWED);
        }

        return responseParser.toResponse(aiReportQuery.save(aiReport));
    }

    @Override
    @Transactional(readOnly = true)
    public AiReportResponse getAiReport(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        AiReport aiReport = getOwnedAiReport(plan, memberId)
                .orElseThrow(() -> new AiReportException(AiReportErrorType.AI_REPORT_NOT_FOUND));

        return responseParser.toResponse(aiReport);
    }

    private Optional<AiReport> getOwnedAiReport(BusinessPlan plan, Long memberId) {

        // 소유자 검증 및 작성 완료 검증
        if (!plan.isOwnedBy(memberId)) {
            throw new AiReportException(AiReportErrorType.UNAUTHORIZED_ACCESS);
        }
        if (!plan.areWritingCompleted()) {
            throw new AiReportException(AiReportErrorType.NOT_READY_FOR_AI_REPORT);
        }

        return aiReportQuery.findByBusinessPlanId(plan.getId());
    }
}
