package starlight.application.aireport.provided.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import starlight.domain.aireport.entity.AiReport;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 리포트 응답 DTO
 * LLM 채점 결과와 API 응답을 모두 담는 통합 DTO
 */
public record AiReportResult(
        Long id,  // null 가능 (LLM 결과 파싱 시에는 null)
        Long businessPlanId,  // null 가능 (LLM 결과 파싱 시에는 null)
        Integer totalScore,
        Integer problemRecognitionScore,
        Integer feasibilityScore,
        Integer growthStrategyScore,
        Integer teamCompetenceScore,
        List<SectionScoreDetailResponse> sectionScores,
        List<StrengthWeakness> strengths,
        List<StrengthWeakness> weaknesses
) {
    public record SectionScoreDetailResponse(
            String sectionType,
            @JsonRawValue String gradingListScores
    ) {
        public static SectionScoreDetailResponse fromJsonNode(JsonNode node) {
            String sectionType = node.path("sectionType").asText("");
            JsonNode gradingListNode = node.path("gradingListScores");
            String gradingListScores;
            if (gradingListNode != null && gradingListNode.isArray()) {
                gradingListScores = gradingListNode.toString();
            } else {
                gradingListScores = gradingListNode != null ? gradingListNode.asText("[]") : "[]";
            }
            if (!gradingListScores.equals("[]") && !gradingListScores.isEmpty() && !gradingListScores.trim().startsWith("[")) {
                gradingListScores = "[]";
            }
            return new SectionScoreDetailResponse(sectionType, gradingListScores);
        }

        /**
         * JsonNode 배열에서 리스트 생성
         */
        public static List<SectionScoreDetailResponse> listFromJsonNode(JsonNode arrayNode) {
            List<SectionScoreDetailResponse> list = new ArrayList<>();
            if (arrayNode == null || !arrayNode.isArray()) {
                return list;
            }
            for (JsonNode node : arrayNode) {
                try {
                    list.add(fromJsonNode(node));
                } catch (Exception e) {
                    // 항목 스킵
                }
            }
            return list;
        }
    }

    public record StrengthWeakness(
            String title,
            String content
    ) {
        /**
         * 단일 JsonNode에서 인스턴스 생성
         */
        public static StrengthWeakness fromJsonNode(JsonNode node) {
            return new StrengthWeakness(
                    node.path("title").asText(""),
                    node.path("content").asText(""));
        }

        /**
         * JsonNode 배열에서 리스트 생성
         */
        public static List<StrengthWeakness> listFromJsonNode(JsonNode arrayNode) {
            List<StrengthWeakness> list = new ArrayList<>();
            if (arrayNode != null && arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    list.add(fromJsonNode(node));
                }
            }
            return list;
        }
    }
    
    /**
     * LLM 결과만으로 AiReportResponse 생성 (id, businessPlanId는 null)
     */
    public static AiReportResult fromGradingResult(
            Integer problemRecognitionScore,
            Integer feasibilityScore,
            Integer growthStrategyScore,
            Integer teamCompetenceScore,
            List<SectionScoreDetailResponse> sectionScores,
            List<StrengthWeakness> strengths,
            List<StrengthWeakness> weaknesses
    ) {
        Integer totalScore = sumTotalScore(problemRecognitionScore, feasibilityScore, growthStrategyScore, teamCompetenceScore);

        return new AiReportResult(
                null,
                null,
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

    private static Integer sumTotalScore(Integer problemRecognitionScore, Integer feasibilityScore, Integer growthStrategyScore, Integer teamCompetenceScore) {
        return (problemRecognitionScore != null ? problemRecognitionScore : 0) +
               (feasibilityScore != null ? feasibilityScore : 0) +
               (growthStrategyScore != null ? growthStrategyScore : 0) +
               (teamCompetenceScore != null ? teamCompetenceScore : 0);
    }

    /**
     * AiReport 엔티티에서 API 응답 DTO로 변환 (id, businessPlanId 포함)
     */
    public static AiReportResult from(AiReport aiReport) {
        JsonNode jsonNode = aiReport.getRawJson().asTree();

        AiReportResult base = fromJsonNode(jsonNode);
        
        Integer totalScore = sumTotalScore(
                base.problemRecognitionScore(),
                base.feasibilityScore(),
                base.growthStrategyScore(),
                base.teamCompetenceScore());
        
        return new AiReportResult(
                aiReport.getId(),
                aiReport.getBusinessPlanId(),
                totalScore,
                base.problemRecognitionScore(),
                base.feasibilityScore(),
                base.growthStrategyScore(),
                base.teamCompetenceScore(),
                base.sectionScores(),
                base.strengths(),
                base.weaknesses());
    }

    /**
     * 저장된 JSON(JsonNode)에서 DTO로 변환 (id, businessPlanId는 null)
     * 엔티티 변환 및 LLM 파싱 결과 조립 시 공통 사용
     */
    public static AiReportResult fromJsonNode(JsonNode jsonNode) {
        Integer problemRecognitionScore = null;
        Integer feasibilityScore = null;
        Integer growthStrategyScore = null;
        Integer teamCompetenceScore = null;

        if (jsonNode.has("problemRecognitionScore") && !jsonNode.path("problemRecognitionScore").isNull()) {
            problemRecognitionScore = jsonNode.path("problemRecognitionScore").asInt();
        }
        if (jsonNode.has("feasibilityScore") && !jsonNode.path("feasibilityScore").isNull()) {
            feasibilityScore = jsonNode.path("feasibilityScore").asInt();
        }
        if (jsonNode.has("growthStrategyScore") && !jsonNode.path("growthStrategyScore").isNull()) {
            growthStrategyScore = jsonNode.path("growthStrategyScore").asInt();
        }
        if (jsonNode.has("teamCompetenceScore") && !jsonNode.path("teamCompetenceScore").isNull()) {
            teamCompetenceScore = jsonNode.path("teamCompetenceScore").asInt();
        }

        List<StrengthWeakness> strengths = StrengthWeakness.listFromJsonNode(jsonNode.path("strengths"));
        List<StrengthWeakness> weaknesses = StrengthWeakness.listFromJsonNode(jsonNode.path("weaknesses"));
        List<SectionScoreDetailResponse> sectionScores = SectionScoreDetailResponse.listFromJsonNode(jsonNode.path("sectionScores"));

        return fromGradingResult(
                problemRecognitionScore,
                feasibilityScore,
                growthStrategyScore,
                teamCompetenceScore,
                sectionScores,
                strengths,
                weaknesses);
    }

    /**
     * 저장용 JsonNode로 변환 (엔티티 raw_json 형식과 동일)
     */
    public JsonNode toJsonNode() {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("problemRecognitionScore", problemRecognitionScore() != null ? problemRecognitionScore() : 0);
        rootNode.put("feasibilityScore", feasibilityScore() != null ? feasibilityScore() : 0);
        rootNode.put("growthStrategyScore", growthStrategyScore() != null ? growthStrategyScore() : 0);
        rootNode.put("teamCompetenceScore", teamCompetenceScore() != null ? teamCompetenceScore() : 0);

        ArrayNode strengthsArray = rootNode.putArray("strengths");
        if (strengths() != null) {
            for (StrengthWeakness s : strengths()) {
                ObjectNode n = strengthsArray.addObject();
                n.put("title", s.title() != null ? s.title() : "");
                n.put("content", s.content() != null ? s.content() : "");
            }
        }
        ArrayNode weaknessesArray = rootNode.putArray("weaknesses");
        if (weaknesses() != null) {
            for (StrengthWeakness w : weaknesses()) {
                ObjectNode n = weaknessesArray.addObject();
                n.put("title", w.title() != null ? w.title() : "");
                n.put("content", w.content() != null ? w.content() : "");
            }
        }
        ArrayNode sectionScoresArray = rootNode.putArray("sectionScores");
        if (sectionScores() != null) {
            for (SectionScoreDetailResponse ss : sectionScores()) {
                ObjectNode n = sectionScoresArray.addObject();
                n.put("sectionType", ss.sectionType() != null ? ss.sectionType() : "");
                n.put("gradingListScores", ss.gradingListScores() != null ? ss.gradingListScores() : "[]");
            }
        }
        return rootNode;
    }

}

