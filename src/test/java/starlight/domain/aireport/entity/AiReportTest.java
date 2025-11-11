package starlight.domain.aireport.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.shared.valueobject.RawJson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiReportTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("AiReport를 정상적으로 생성할 수 있다")
    void create_success() {
        // given
        Long businessPlanId = 1L;
        String rawJson = "{\"totalScore\": 100, \"problemRecognitionScore\": 20}";

        // when
        AiReport aiReport = AiReport.create(businessPlanId, rawJson);

        // then
        assertThat(aiReport).isNotNull();
        assertThat(aiReport.getBusinessPlanId()).isEqualTo(businessPlanId);
        assertThat(aiReport.getRawJson()).isNotNull();
        assertThat(aiReport.getRawJson().getValue()).isEqualTo(
                RawJson.create(rawJson).getValue()
        );
    }

    @Test
    @DisplayName("businessPlanId가 null이면 예외가 발생한다")
    void create_withNullBusinessPlanId_throwsException() {
        // given
        String rawJson = "{\"totalScore\": 100}";

        // when & then
        assertThatThrownBy(() -> AiReport.create(null, rawJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("businessPlanId must not be null");
    }

    @Test
    @DisplayName("rawJson이 null이면 예외가 발생한다")
    void create_withNullRawJson_throwsException() {
        // given
        Long businessPlanId = 1L;

        // when & then
        assertThatThrownBy(() -> AiReport.create(businessPlanId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rawJson은 null일 수 없습니다");
    }

    @Test
    @DisplayName("복잡한 JSON 구조도 정상적으로 저장된다")
    void create_withComplexJson_success() throws Exception {
        // given
        Long businessPlanId = 1L;
        String complexJson = """
                {
                  "problemRecognitionScore": 20,
                  "feasibilityScore": 30,
                  "growthStrategyScore": 30,
                  "teamCompetenceScore": 20,
                  "strengths": [
                    {"title": "강점 1", "content": "내용 1"},
                    {"title": "강점 2", "content": "내용 2"},
                    {"title": "강점 3", "content": "내용 3"}
                  ],
                  "weaknesses": [
                    {"title": "약점 1", "content": "내용 1"},
                    {"title": "약점 2", "content": "내용 2"},
                    {"title": "약점 3", "content": "내용 3"}
                  ],
                  "sectionScores": [
                    {
                      "sectionType": "PROBLEM_RECOGNITION",
                      "gradingListScores": "[{\\"item\\": \\"질문1\\", \\"score\\": 4}]"
                    }
                  ]
                }
                """;

        // when
        AiReport aiReport = AiReport.create(businessPlanId, complexJson);

        // then
        assertThat(aiReport).isNotNull();
        assertThat(aiReport.getRawJson()).isNotNull();
        
        // RawJson이 정규화되었는지 확인
        RawJson rawJson = aiReport.getRawJson();
        assertThat(rawJson.asTree()).isNotNull();
        
        // JSON 구조 검증
        var jsonNode = rawJson.asTree();
        assertThat(jsonNode.path("problemRecognitionScore").asInt()).isEqualTo(20);
        assertThat(jsonNode.path("strengths").isArray()).isTrue();
        assertThat(jsonNode.path("strengths").size()).isEqualTo(3);
    }

    @Test
    @DisplayName("AiReport의 rawJson을 업데이트할 수 있다")
    void update_success() {
        // given
        Long businessPlanId = 1L;
        String initialJson = "{\"totalScore\": 100}";
        AiReport aiReport = AiReport.create(businessPlanId, initialJson);
        
        String updatedJson = "{\"totalScore\": 150, \"problemRecognitionScore\": 20}";

        // when
        aiReport.update(updatedJson);

        // then
        assertThat(aiReport.getRawJson().getValue()).isEqualTo(
                RawJson.create(updatedJson).getValue()
        );
    }

    @Test
    @DisplayName("update 시 rawJson이 null이면 예외가 발생한다")
    void update_withNullRawJson_throwsException() {
        // given
        Long businessPlanId = 1L;
        String initialJson = "{\"totalScore\": 100}";
        AiReport aiReport = AiReport.create(businessPlanId, initialJson);

        // when & then
        assertThatThrownBy(() -> aiReport.update(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rawJson은 null일 수 없습니다");
    }

    @Test
    @DisplayName("업데이트 후에도 JSON 구조가 올바르게 유지된다")
    void update_preservesJsonStructure() throws Exception {
        // given
        Long businessPlanId = 1L;
        String initialJson = "{\"totalScore\": 100}";
        AiReport aiReport = AiReport.create(businessPlanId, initialJson);
        
        String updatedJson = """
                {
                  "problemRecognitionScore": 18,
                  "feasibilityScore": 25,
                  "growthStrategyScore": 28,
                  "teamCompetenceScore": 19,
                  "sectionScores": [
                    {
                      "sectionType": "FEASIBILITY",
                      "gradingListScores": "[{\\"item\\": \\"질문1\\", \\"score\\": 3}]"
                    }
                  ]
                }
                """;

        // when
        aiReport.update(updatedJson);

        // then
        var jsonNode = aiReport.getRawJson().asTree();
        assertThat(jsonNode.path("problemRecognitionScore").asInt()).isEqualTo(18);
        assertThat(jsonNode.path("feasibilityScore").asInt()).isEqualTo(25);
        assertThat(jsonNode.path("sectionScores").isArray()).isTrue();
        assertThat(jsonNode.path("sectionScores").get(0)
                .path("sectionType").asText()).isEqualTo("FEASIBILITY");
    }

    @Test
    @DisplayName("RawJson이 정규화되어 저장된다")
    void create_normalizesJson() {
        // given
        Long businessPlanId = 1L;
        // 공백과 들여쓰기가 다른 JSON
        String rawJson = "{\"totalScore\":100,\"problemRecognitionScore\":20}";
        String formattedJson = """
                {
                  "totalScore": 100,
                  "problemRecognitionScore": 20
                }
                """;

        // when
        AiReport aiReport1 = AiReport.create(businessPlanId, rawJson);
        AiReport aiReport2 = AiReport.create(businessPlanId, formattedJson);

        // then
        // RawJson이 정규화되어 같은 값으로 저장됨
        assertThat(aiReport1.getRawJson().getValue())
                .isEqualTo(aiReport2.getRawJson().getValue());
    }
}

