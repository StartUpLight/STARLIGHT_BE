package starlight.application.aireport.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.application.aireport.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.aireport.exception.AiReportErrorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AiReportResponseParser 테스트")
class AiReportResponseParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiReportResponseParser parser = new AiReportResponseParser(objectMapper);

    @Test
    @DisplayName("유효한 JSON 응답을 파싱한다")
    void parse_validJson_returnsResponse() {
        // given
        String validJson = """
                {
                    "problemRecognitionScore": 18,
                    "feasibilityScore": 28,
                    "growthStrategyScore": 30,
                    "teamCompetenceScore": 20,
                    "strengths": [
                        {"title": "강점1", "content": "내용1"}
                    ],
                    "weaknesses": [
                        {"title": "약점1", "content": "내용1"}
                    ],
                    "sectionScores": [
                        {
                            "sectionType": "PROBLEM_RECOGNITION",
                            "gradingListScores": "[{\\"item\\":\\"항목1\\",\\"score\\":5,\\"maxScore\\":5}]"
                        }
                    ]
                }
                """;

        // when
        AiReportResult result = parser.parse(validJson);

        // then
        assertThat(result).isNotNull();
        assertThat(result.problemRecognitionScore()).isEqualTo(18);
        assertThat(result.feasibilityScore()).isEqualTo(28);
        assertThat(result.growthStrategyScore()).isEqualTo(30);
        assertThat(result.teamCompetenceScore()).isEqualTo(20);
        assertThat(result.strengths()).hasSize(1);
        assertThat(result.weaknesses()).hasSize(1);
        assertThat(result.sectionScores()).hasSize(1);
    }

    @Test
    @DisplayName("null 응답 시 예외를 던진다")
    void parse_nullResponse_throwsException() {
        // when & then
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
    }

    @Test
    @DisplayName("빈 문자열 응답 시 예외를 던진다")
    void parse_emptyResponse_throwsException() {
        // when & then
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
    }

    @Test
    @DisplayName("필수 필드가 없는 응답 시 예외를 던진다")
    void parse_missingRequiredFields_throwsException() {
        // given
        String invalidJson = """
                {
                    "strengths": [],
                    "weaknesses": []
                }
                """;

        // when & then
        assertThatThrownBy(() -> parser.parse(invalidJson))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
    }

    @Test
    @DisplayName("기본값(모두 0) 응답 시 예외를 던진다")
    void parse_defaultResponse_throwsException() {
        // given
        String defaultJson = """
                {
                    "problemRecognitionScore": 0,
                    "feasibilityScore": 0,
                    "growthStrategyScore": 0,
                    "teamCompetenceScore": 0,
                    "strengths": [],
                    "weaknesses": [],
                    "sectionScores": []
                }
                """;

        // when & then
        assertThatThrownBy(() -> parser.parse(defaultJson))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
    }

    @Test
    @DisplayName("text 필드가 있는 응답을 파싱한다")
    void parse_textFieldResponse_parsesCorrectly() {
        // given
        String textFieldJson = """
                {
                    "text": "{\\"problemRecognitionScore\\": 18, \\"feasibilityScore\\": 28, \\"growthStrategyScore\\": 30, \\"teamCompetenceScore\\": 20, \\"strengths\\": [], \\"weaknesses\\": [], \\"sectionScores\\": []}"
                }
                """;

        // when
        AiReportResult result = parser.parse(textFieldJson);

        // then
        assertThat(result).isNotNull();
        assertThat(result.problemRecognitionScore()).isEqualTo(18);
        assertThat(result.feasibilityScore()).isEqualTo(28);
    }

    @Test
    @DisplayName("잘못된 JSON 형식 시 예외를 던진다")
    void parse_invalidJson_throwsException() {
        // given
        String invalidJson = "not a json";

        // when & then
        assertThatThrownBy(() -> parser.parse(invalidJson))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.AI_RESPONSE_PARSING_FAILED);
    }
}

