package starlight.application.aireport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.aireport.dto.AiReportResponse;
import starlight.application.aireport.provided.AiReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import starlight.shared.valueobject.RawJson;
import starlight.application.aireport.required.AiReportGrader;
import starlight.application.aireport.required.AiReportQuery;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.aireport.exception.AiReportErrorType;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiReportServiceImpl implements AiReportService {

    private final BusinessPlanQuery businessPlanQuery;
    private final AiReportQuery aiReportQuery;
    private final AiReportGrader aiReportGrader;
    private final ObjectMapper objectMapper;

    @Override
    public AiReportResponse gradeBusinessPlan(Long businessPlanId, Long memberId) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(businessPlanId);

        // 소유자 검증 및 작성 완료 검증
        if (!businessPlan.isOwnedBy(memberId)) {
            throw new AiReportException(AiReportErrorType.UNAUTHORIZED_ACCESS);
        }
        if (!businessPlan.areWritingCompleted()) {
            throw new AiReportException(AiReportErrorType.NOT_READY_FOR_AI_REPORT);
        }

        // LLM 채점
        AiReportResponse gradingResult = aiReportGrader.grade(businessPlan);

        // AiReportResponse를 JsonNode로 변환하여 RawJson
        JsonNode gradingJsonNode = convertToJsonNode(gradingResult);

        // AiReport 생성 또는 업데이트
        Optional<AiReport> existingReport = aiReportQuery.findByBusinessPlanId(businessPlanId);
        AiReport aiReport;

        if (existingReport.isPresent()) {
            aiReport = existingReport.get();
            aiReport.update(RawJson.create(gradingJsonNode).getValue());
        } else {
            aiReport = AiReport.create(businessPlanId, RawJson.create(gradingJsonNode).getValue());
            businessPlan.updateStatus(PlanStatus.AI_REVIEWED);
        }

        return toResponse(aiReportQuery.save(aiReport));
    }

    @Override
    @Transactional(readOnly = true)
    public AiReportResponse getAiReport(Long businessPlanId, Long memberId) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(businessPlanId);

        if (!businessPlan.isOwnedBy(memberId)) {
            throw new AiReportException(AiReportErrorType.UNAUTHORIZED_ACCESS);
        }

        AiReport aiReport = aiReportQuery.findByBusinessPlanId(businessPlanId)
                .orElseThrow(() -> new AiReportException(AiReportErrorType.AI_REPORT_NOT_FOUND));

        return toResponse(aiReport);
    }

    /**
     * AiReportResponse를 JsonNode로 변환 (저장용)
     * 또는 JsonNode에서 AiReportResponse로 변환 (조회용)
     * 통합된 변환 메소드
     */
    private JsonNode convertToJsonNode(AiReportResponse response) {
        ObjectNode rootNode = objectMapper.createObjectNode();

        // 점수 필드
        rootNode.put("problemRecognitionScore",
                response.problemRecognitionScore() != null ? response.problemRecognitionScore() : 0);
        rootNode.put("feasibilityScore",
                response.feasibilityScore() != null ? response.feasibilityScore() : 0);
        rootNode.put("growthStrategyScore",
                response.growthStrategyScore() != null ? response.growthStrategyScore() : 0);
        rootNode.put("teamCompetenceScore",
                response.teamCompetenceScore() != null ? response.teamCompetenceScore() : 0);

        // 강점 배열
        ArrayNode strengthsArray = rootNode.putArray("strengths");
        if (response.strengths() != null) {
            for (AiReportResponse.StrengthWeakness strength : response.strengths()) {
                ObjectNode strengthNode = strengthsArray.addObject();
                strengthNode.put("title", strength.title() != null ? strength.title() : "");
                strengthNode.put("content", strength.content() != null ? strength.content() : "");
            }
        }

        // 약점 배열
        ArrayNode weaknessesArray = rootNode.putArray("weaknesses");
        if (response.weaknesses() != null) {
            for (AiReportResponse.StrengthWeakness weakness : response.weaknesses()) {
                ObjectNode weaknessNode = weaknessesArray.addObject();
                weaknessNode.put("title", weakness.title() != null ? weakness.title() : "");
                weaknessNode.put("content", weakness.content() != null ? weakness.content() : "");
            }
        }

        // 섹션별 점수 배열: sectionType과 gradingListScores
        ArrayNode sectionScoresArray = rootNode.putArray("sectionScores");
        if (response.sectionScores() != null) {
            for (AiReportResponse.SectionScoreDetailResponse sectionScore : response.sectionScores()) {
                ObjectNode sectionScoreNode = sectionScoresArray.addObject();
                sectionScoreNode.put("sectionType",
                        sectionScore.sectionType() != null ? sectionScore.sectionType() : "");
                sectionScoreNode.put("gradingListScores",
                        sectionScore.gradingListScores() != null ? sectionScore.gradingListScores() : "[]");
            }
        }

        return rootNode;
    }

    /**
     * AiReport에서 AiReportResponse로 변환
     */
    private AiReportResponse toResponse(AiReport aiReport) {
        JsonNode jsonNode = aiReport.getRawJson().asTree();

        // 점수 추출
        Integer problemRecognitionScore = jsonNode.path("problemRecognitionScore").asInt(0);
        Integer feasibilityScore = jsonNode.path("feasibilityScore").asInt(0);
        Integer growthStrategyScore = jsonNode.path("growthStrategyScore").asInt(0);
        Integer teamCompetenceScore = jsonNode.path("teamCompetenceScore").asInt(0);
        Integer totalScore = problemRecognitionScore + feasibilityScore + growthStrategyScore + teamCompetenceScore;

        // 강점 파싱
        List<AiReportResponse.StrengthWeakness> strengths = new ArrayList<>();
        JsonNode strengthsNode = jsonNode.path("strengths");
        if (strengthsNode.isArray()) {
            for (JsonNode strengthNode : strengthsNode) {
                strengths.add(new AiReportResponse.StrengthWeakness(
                        strengthNode.path("title").asText(""),
                        strengthNode.path("content").asText("")));
            }
        }

        // 약점 파싱
        List<AiReportResponse.StrengthWeakness> weaknesses = new ArrayList<>();
        JsonNode weaknessesNode = jsonNode.path("weaknesses");
        if (weaknessesNode.isArray()) {
            for (JsonNode weaknessNode : weaknessesNode) {
                weaknesses.add(new AiReportResponse.StrengthWeakness(
                        weaknessNode.path("title").asText(""),
                        weaknessNode.path("content").asText("")));
            }
        }

        // 섹션별 세부 배점 파싱: sectionType과 gradingListScores만 포함
        List<AiReportResponse.SectionScoreDetailResponse> sectionScores = new ArrayList<>();
        JsonNode sectionScoresNode = jsonNode.path("sectionScores");
        if (sectionScoresNode.isArray()) {
            for (JsonNode sectionScoreNode : sectionScoresNode) {
                sectionScores.add(new AiReportResponse.SectionScoreDetailResponse(
                        sectionScoreNode.path("sectionType").asText(""),
                        sectionScoreNode.path("gradingListScores").asText("[]")));
            }
        }

        return new AiReportResponse(
                aiReport.getId(),
                aiReport.getBusinessPlanId(),
                totalScore,
                problemRecognitionScore,
                feasibilityScore,
                growthStrategyScore,
                teamCompetenceScore,
                sectionScores,
                strengths,
                weaknesses
        );
    }
}
