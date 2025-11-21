package starlight.adapter.ai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.aireport.provided.dto.AiReportResponse;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.aireport.exception.AiReportErrorType;

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
     * 응답이 기본값(파싱 실패 시 반환되는 값)인지 확인
     */
    private boolean isDefaultResponse(AiReportResponse response) {
        return (response.problemRecognitionScore() == null || response.problemRecognitionScore() == 0) &&
               (response.feasibilityScore() == null || response.feasibilityScore() == 0) &&
               (response.growthStrategyScore() == null || response.growthStrategyScore() == 0) &&
               (response.teamCompetenceScore() == null || response.teamCompetenceScore() == 0) &&
               (response.strengths() == null || response.strengths().isEmpty()) &&
               (response.weaknesses() == null || response.weaknesses().isEmpty()) &&
               (response.sectionScores() == null || response.sectionScores().isEmpty());
    }

    /**
     * LLM 응답 문자열을 AiReportResponse로 파싱
     * 파싱 실패 시 예외를 던집니다.
     */
    public AiReportResponse parse(String llmResponse) {
        log.debug("Raw LLM response: {}", llmResponse);
        
        // 1. 기본 검증
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            log.error("LLM response is null or empty");
            throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
        }
        
        try {
            // 2. JSON 문자열 정리
            String cleanedJson = cleanJsonResponse(llmResponse);
            log.debug("Cleaned JSON: {}", cleanedJson);
            
            // 3. JSON 파싱 시도
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);
            
            // 4. 필수 필드 존재 여부 확인
            if (!jsonNode.has("problemRecognitionScore") ||
                !jsonNode.has("feasibilityScore") ||
                !jsonNode.has("growthStrategyScore") ||
                !jsonNode.has("teamCompetenceScore")) {
                throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
            }
            
            // 5. 파싱 시도
            AiReportResponse response = parseFromJsonNode(jsonNode);
            
            // 6. 파싱된 값이 기본값인지 확인
            if (isDefaultResponse(response)) {
                log.error("Parsed response is default (all zeros), likely parsing failure");
                throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
            }
            
            return response;
        } catch (Exception e) {
            log.error("Failed to parse LLM response. Response: {}", llmResponse, e);
            throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
        }
    }

    /**
     * JSON 응답 문자열 정리 및 복구
     */
    private String cleanJsonResponse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "{}";
        }
        
        String cleaned = json.trim();
        
        // 1. JSON 코드 블록 마커 제거 (```json ... ``` 또는 ``` ... ```)
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();
        
        // 2. "text" 필드에서 JSON 추출 (더 강력한 추출)
        // 정규식으로 "text" 필드 추출 시도
        if (cleaned.contains("\"text\"") || cleaned.contains("'text'")) {
            try {
                // 먼저 JSON 파싱 시도
                JsonNode root = objectMapper.readTree(cleaned);
                if (root.has("text") && root.get("text").isTextual()) {
                    cleaned = root.get("text").asText();
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 정규식으로 추출 시도
                try {
                    // "text" : "..." 패턴 찾기
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "\"text\"\\s*:\\s*\"(.*)\"", 
                        java.util.regex.Pattern.DOTALL
                    );
                    java.util.regex.Matcher matcher = pattern.matcher(cleaned);
                    if (matcher.find()) {
                        String extracted = matcher.group(1);
                        // 이스케이프된 문자 처리
                        extracted = extracted.replace("\\n", "\n")
                                            .replace("\\\"", "\"")
                                            .replace("\\\\", "\\");
                        cleaned = extracted;
                        log.debug("Extracted text field using regex");
                    }
                } catch (Exception e2) {
                    log.warn("Failed to extract text field using regex: {}", e2.getMessage());
                }
            }
        }

        // 3. 잘못된 따옴표 패턴 수정 (공백이 포함된 필드명)
        cleaned = cleaned.replaceAll("\"\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s+\"", "\"$1\"");

        return cleaned;
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
     * 불완전한 항목은 건너뛰거나 기본값으로 대체
     */
    private List<AiReportResponse.SectionScoreDetailResponse> parseSectionScores(JsonNode node) {
        List<AiReportResponse.SectionScoreDetailResponse> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode sectionScoreNode : node) {
                try {
                    String sectionType = sectionScoreNode.path("sectionType").asText("");
                    String gradingListScores = sectionScoreNode.path("gradingListScores").asText("[]");
                    
                    // gradingListScores가 유효한 JSON 문자열인지 검증
                    if (!gradingListScores.equals("[]")) {
                        try {
                            // JSON 배열 형식인지 확인
                            if (!gradingListScores.trim().startsWith("[")) {
                                log.warn("Invalid gradingListScores format for sectionType: {}, using default", sectionType);
                                gradingListScores = "[]";
                            } else {
                                // JSON 파싱 가능 여부 확인
                                objectMapper.readTree(gradingListScores);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse gradingListScores for sectionType: {}, using default. Value: {}", 
                                    sectionType, gradingListScores);
                            gradingListScores = "[]";
                        }
                    }
                    
                    list.add(new AiReportResponse.SectionScoreDetailResponse(sectionType, gradingListScores));
                } catch (Exception e) {
                    log.warn("Failed to parse sectionScore item, skipping: {}", e.getMessage());
                    // 불완전한 항목은 건너뛰기
                }
            }
        }
        return list;
    }

}
