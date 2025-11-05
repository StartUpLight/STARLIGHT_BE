package starlight.adapter.ai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.aireport.dto.AiReportResponse;

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
     * LLM 응답 문자열을 AiReportResponse로 파싱
     */
    public AiReportResponse parse(String llmResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(llmResponse);
            return parseAiReportResponse(jsonNode);
        } catch (Exception e) {
            log.error("Failed to parse LLM grading result. output={}", llmResponse, e);
            return createDefaultAiReportResponse();
        }
    }

    /**
     * JsonNode를 파싱하여 AiReportResponse로 변환
     */
    private AiReportResponse parseAiReportResponse(JsonNode jsonNode) {
        Integer problemRecognitionScore = jsonNode.path("problemRecognitionScore").asInt(0);
        Integer feasibilityScore = jsonNode.path("feasibilityScore").asInt(0);
        Integer growthStrategyScore = jsonNode.path("growthStrategyScore").asInt(0);
        Integer teamCompetenceScore = jsonNode.path("teamCompetenceScore").asInt(0);

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

        // sectionScores 파싱: sectionType과 gradingListScores만 포함
        List<AiReportResponse.SectionScoreDetailResponse> sectionScores = new ArrayList<>();
        JsonNode sectionScoresNode = jsonNode.path("sectionScores");
        if (sectionScoresNode.isArray()) {
            for (JsonNode sectionScoreNode : sectionScoresNode) {
                sectionScores.add(new AiReportResponse.SectionScoreDetailResponse(
                        sectionScoreNode.path("sectionType").asText(""),
                        sectionScoreNode.path("gradingListScores").asText("[]")));
            }
        }

        return AiReportResponse.fromGradingResult(
                problemRecognitionScore,
                feasibilityScore,
                growthStrategyScore,
                teamCompetenceScore,
                sectionScores,
                strengths,
                weaknesses
        );
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

