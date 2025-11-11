package starlight.adapter.ai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.aireport.dto.AiReportResponse;
import starlight.domain.aireport.entity.AiReport;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 응답을 파싱하여 AiReportResponse로 변환하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * AiReportResponse를 JsonNode로 변환 (저장용)
     * 또는 JsonNode에서 AiReportResponse로 변환 (조회용)
     * 통합된 변환 메소드
     */
    public JsonNode convertToJsonNode(AiReportResponse response) {
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
     * 파싱 로직은 AiReportResponseParser를 재사용하고, id와 businessPlanId만 추가
     */
    public AiReportResponse toResponse(AiReport aiReport) {
        JsonNode jsonNode = aiReport.getRawJson().asTree();

        // 공통 파싱 로직 재사용
        AiReportResponse baseResponse = parseFromJsonNode(jsonNode);

        // totalScore 계산
        Integer totalScore = (baseResponse.problemRecognitionScore() != null ? baseResponse.problemRecognitionScore() : 0) +
                (baseResponse.feasibilityScore() != null ? baseResponse.feasibilityScore() : 0) +
                (baseResponse.growthStrategyScore() != null ? baseResponse.growthStrategyScore() : 0) +
                (baseResponse.teamCompetenceScore() != null ? baseResponse.teamCompetenceScore() : 0);

        // id와 businessPlanId를 포함하여 새 인스턴스 생성
        return new AiReportResponse(
                aiReport.getId(),
                aiReport.getBusinessPlanId(),
                totalScore,
                baseResponse.problemRecognitionScore(),
                baseResponse.feasibilityScore(),
                baseResponse.growthStrategyScore(),
                baseResponse.teamCompetenceScore(),
                baseResponse.sectionScores(),
                baseResponse.strengths(),
                baseResponse.weaknesses()
        );
    }

    /**
     * LLM 응답 문자열을 AiReportResponse로 파싱
     */
    public AiReportResponse parse(String llmResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(llmResponse);
            return parseFromJsonNode(jsonNode);
        } catch (Exception e) {
            return createDefaultAiReportResponse();
        }
    }

    /**
     * JsonNode를 파싱하여 AiReportResponse로 변환
     */
    private AiReportResponse parseFromJsonNode(JsonNode jsonNode) {
        Integer problemRecognitionScore = jsonNode.path("problemRecognitionScore").asInt(0);
        Integer feasibilityScore = jsonNode.path("feasibilityScore").asInt(0);
        Integer growthStrategyScore = jsonNode.path("growthStrategyScore").asInt(0);
        Integer teamCompetenceScore = jsonNode.path("teamCompetenceScore").asInt(0);

        // 강점 파싱
        List<AiReportResponse.StrengthWeakness> strengths = parseStrengthWeaknessList(jsonNode.path("strengths"));

        // 약점 파싱
        List<AiReportResponse.StrengthWeakness> weaknesses = parseStrengthWeaknessList(jsonNode.path("weaknesses"));

        // sectionScores 파싱: sectionType과 gradingListScores만 포함
        List<AiReportResponse.SectionScoreDetailResponse> sectionScores = parseSectionScores(
                jsonNode.path("sectionScores"));

        return AiReportResponse.fromGradingResult(
                problemRecognitionScore,
                feasibilityScore,
                growthStrategyScore,
                teamCompetenceScore,
                sectionScores,
                strengths,
                weaknesses);
    }

    /**
     * 강점/약점 리스트 파싱
     */
    private List<AiReportResponse.StrengthWeakness> parseStrengthWeaknessList(JsonNode node) {
        List<AiReportResponse.StrengthWeakness> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode itemNode : node) {
                list.add(new AiReportResponse.StrengthWeakness(
                        itemNode.path("title").asText(""),
                        itemNode.path("content").asText("")));
            }
        }
        return list;
    }

    /**
     * 섹션 점수 리스트 파싱
     */
    private List<AiReportResponse.SectionScoreDetailResponse> parseSectionScores(JsonNode node) {
        List<AiReportResponse.SectionScoreDetailResponse> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode sectionScoreNode : node) {
                list.add(new AiReportResponse.SectionScoreDetailResponse(
                        sectionScoreNode.path("sectionType").asText(""),
                        sectionScoreNode.path("gradingListScores").asText("[]")));
            }
        }
        return list;
    }

    /**
     * 기본값 AiReportResponse 생성 (파싱 실패 시 사용)
     */
    private AiReportResponse createDefaultAiReportResponse() {
        return AiReportResponse.fromGradingResult(
                0, 0, 0, 0,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
    }
}
