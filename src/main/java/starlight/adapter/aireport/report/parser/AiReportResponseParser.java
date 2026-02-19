package starlight.adapter.aireport.report.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.aireport.exception.AiReportErrorType;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * LLM 응답 문자열을 AiReportResponse로 파싱 (전체 리포트용)
     * 4개의 전체 점수 필드를 모두 요구
     */
    public AiReportResult parse(String llmResponse) {

        // 1. 기본 검증
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            log.error("LLM response is null or empty");
            throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
        }

        try {
            // 2. JSON 문자열 정리
            String cleanedJson = cleanJsonResponse(llmResponse);

            // 3. JSON 파싱 시도
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);

            // 4. 필수 필드 존재 여부 확인 (전체 리포트는 4개 필드 모두 필요)
            if (!jsonNode.has("problemRecognitionScore") ||
                    !jsonNode.has("feasibilityScore") ||
                    !jsonNode.has("growthStrategyScore") ||
                    !jsonNode.has("teamCompetenceScore")) {
                throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
            }

            // 5. 파싱 시도
            AiReportResult response = AiReportResult.fromJsonNode(jsonNode);

            // 6. 파싱된 값이 기본값인지 확인
            if (isDefaultResponse(response)) {
                log.error("Parsed response is default (all zeros), likely parsing failure");
                throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
            }

            return response;
        } catch (Exception e) {
            throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
        }
    }

    /**
     * 섹션별 채점 응답을 파싱 (섹션별 Agent용)
     * 하나의 섹션 점수만 포함하는 응답을 처리합니다.
     * 예: {"feasibilityScore": 0, "sectionScores": [...]}
     */
    public AiReportResult parseSectionResponse(String llmResponse) {

        // 1. 기본 검증
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            log.error("Section LLM response is null or empty");
            throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
        }

        try {
            // 2. JSON 문자열 정리
            String cleanedJson = cleanJsonResponse(llmResponse);

            // 3. JSON 파싱 시도
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);

            // 4. 섹션별 응답은 하나의 점수 필드만 있으면 됨
            // 어떤 섹션 점수 필드가 있는지 확인
            Integer problemRecognitionScore = null;
            Integer feasibilityScore = null;
            Integer growthStrategyScore = null;
            Integer teamCompetenceScore = null;

            if (jsonNode.has("problemRecognitionScore") && !jsonNode.path("problemRecognitionScore").isNull()) {
                problemRecognitionScore = jsonNode.path("problemRecognitionScore").asInt(0);
            }
            if (jsonNode.has("feasibilityScore") && !jsonNode.path("feasibilityScore").isNull()) {
                feasibilityScore = jsonNode.path("feasibilityScore").asInt(0);
            }
            if (jsonNode.has("growthStrategyScore") && !jsonNode.path("growthStrategyScore").isNull()) {
                growthStrategyScore = jsonNode.path("growthStrategyScore").asInt(0);
            }
            if (jsonNode.has("teamCompetenceScore") && !jsonNode.path("teamCompetenceScore").isNull()) {
                teamCompetenceScore = jsonNode.path("teamCompetenceScore").asInt(0);
            }

            // 최소 하나의 점수 필드는 있어야 함
            if (problemRecognitionScore == null && feasibilityScore == null
                    && growthStrategyScore == null && teamCompetenceScore == null) {
                throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
            }

            // 5. 섹션별 응답 파싱 (없는 필드는 null로 설정)
            List<AiReportResult.SectionScoreDetailResponse> sectionScores = AiReportResult.fromJsonNode(jsonNode).sectionScores();

            // strengths와 weaknesses는 섹션별 응답에는 없음
            return AiReportResult.fromGradingResult(
                    problemRecognitionScore,
                    feasibilityScore,
                    growthStrategyScore,
                    teamCompetenceScore,
                    sectionScores,
                    List.of(), // strengths는 빈 리스트
                    List.of() // weaknesses는 빈 리스트
            );

        } catch (Exception e) {
            throw new AiReportException(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
        }
    }

    /**
     * 강점/약점 리스트 파싱 (슈퍼바이저용)
     */
    public List<AiReportResult.StrengthWeakness> parseStrengthWeakness(String llmResponse, String type) {
        try {
            String cleanedJson = cleanJsonResponse(llmResponse);
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);

            JsonNode targetNode = jsonNode.path(type);
            return AiReportResult.StrengthWeakness.listFromJsonNode(targetNode);
        } catch (Exception e) {
            log.error("Failed to parse strength/weakness from supervisor response. Type: {}", type, e);
            return List.of();
        }
    }

    /**
     * 응답이 기본값(파싱 실패 시 반환되는 값)인지 확인
     */
    private boolean isDefaultResponse(AiReportResult response) {
        return (response.problemRecognitionScore() == null || response.problemRecognitionScore() == 0) &&
                (response.feasibilityScore() == null || response.feasibilityScore() == 0) &&
                (response.growthStrategyScore() == null || response.growthStrategyScore() == 0) &&
                (response.teamCompetenceScore() == null || response.teamCompetenceScore() == 0) &&
                (response.strengths() == null || response.strengths().isEmpty()) &&
                (response.weaknesses() == null || response.weaknesses().isEmpty()) &&
                (response.sectionScores() == null || response.sectionScores().isEmpty());
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
                            java.util.regex.Pattern.DOTALL);
                    java.util.regex.Matcher matcher = pattern.matcher(cleaned);
                    if (matcher.find()) {
                        String extracted = matcher.group(1);
                        // 이스케이프된 문자 처리
                        extracted = extracted.replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\");
                        cleaned = extracted;
                    }
                } catch (Exception e2) {
                }
            }
        }

        // 3. 잘못된 따옴표 패턴 수정 (공백이 포함된 필드명)
        cleaned = cleaned.replaceAll("\"\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s+\"", "\"$1\"");

        // 4. 불완전한 JSON 복구 (닫히지 않은 배열/객체 감지 및 복구)
        cleaned = repairIncompleteJson(cleaned);

        return cleaned;
    }

    /**
     * 불완전한 JSON을 복구 (닫히지 않은 배열/객체 감지 및 복구)
     */
    private String repairIncompleteJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }

        // 괄호 균형 확인
        int openBraces = 0; // {
        int closeBraces = 0; // }
        int openBrackets = 0; // [
        int closeBrackets = 0; // ]

        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            switch (c) {
                case '{':
                    openBraces++;
                    break;
                case '}':
                    closeBraces++;
                    break;
                case '[':
                    openBrackets++;
                    break;
                case ']':
                    closeBrackets++;
                    break;
            }
        }

        // 닫히지 않은 괄호 추가
        StringBuilder repaired = new StringBuilder(json);
        int missingCloseBrackets = openBrackets - closeBrackets;
        int missingCloseBraces = openBraces - closeBraces;

        // 배열을 먼저 닫고, 그 다음 객체를 닫음
        for (int i = 0; i < missingCloseBrackets; i++) {
            repaired.append(']');
        }
        for (int i = 0; i < missingCloseBraces; i++) {
            repaired.append('}');
        }

        if (missingCloseBrackets > 0 || missingCloseBraces > 0) {
            log.warn("불완전한 JSON 감지 및 복구. 누락된 괄호: ] {}개, }} {}개",
                    missingCloseBrackets, missingCloseBraces);
        }

        return repaired.toString();
    }
}
