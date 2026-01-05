package starlight.adapter.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.aireport.report.SpringAiReportGrader;
import starlight.adapter.aireport.report.agent.impl.SpringAiFullReportGradeAgent;
import starlight.adapter.aireport.report.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OpenAiReportGrader 테스트")
class OpenAiReportGraderTest {

    @Test
    @DisplayName("컨텐츠를 채점하여 AiReportResponse를 반환한다")
    void gradeContent_returnsAiReportResponse() {
        // given
        String content = "사업계획서 내용";
        String llmResponse = """
                {
                    "problemRecognitionScore": 20,
                    "feasibilityScore": 25,
                    "growthStrategyScore": 30,
                    "teamCompetenceScore": 20,
                    "sectionScores": [
                        {
                            "sectionType": "PROBLEM_RECOGNITION",
                            "gradingListScores": "[{\\"item\\":\\"항목1\\",\\"score\\":5,\\"maxScore\\":5}]"
                        }
                    ],
                    "strengths": [
                        {"title": "강점1", "content": "내용1"}
                    ],
                    "weaknesses": [
                        {"title": "약점1", "content": "내용1"}
                    ]
                }
                """;

        SpringAiFullReportGradeAgent generator = mock(SpringAiFullReportGradeAgent.class);
        when(generator.generateReport(content)).thenReturn(llmResponse);

        AiReportResponseParser parser = mock(AiReportResponseParser.class);
        AiReportResult expectedResponse = AiReportResult.fromGradingResult(
                20, 25, 30, 20,
                List.of(new AiReportResult.SectionScoreDetailResponse("PROBLEM_RECOGNITION", "[{\"item\":\"항목1\",\"score\":5,\"maxScore\":5}]")),
                List.of(new AiReportResult.StrengthWeakness("강점1", "내용1")),
                List.of(new AiReportResult.StrengthWeakness("약점1", "내용1"))
        );
        when(parser.parse(llmResponse)).thenReturn(expectedResponse);

        SpringAiReportGrader sut = new SpringAiReportGrader(generator, parser);

        // when
        AiReportResult result = sut.gradeWithSectionAgents(content);

        // then
        assertThat(result).isNotNull();
        assertThat(result.problemRecognitionScore()).isEqualTo(20);
        assertThat(result.feasibilityScore()).isEqualTo(25);
        assertThat(result.growthStrategyScore()).isEqualTo(30);
        assertThat(result.teamCompetenceScore()).isEqualTo(20);
        assertThat(result.totalScore()).isEqualTo(95);
        assertThat(result.strengths()).hasSize(1);
        assertThat(result.weaknesses()).hasSize(1);
        assertThat(result.sectionScores()).hasSize(1);

        verify(generator).generateReport(content);
        verify(parser).parse(llmResponse);
    }

    @Test
    @DisplayName("각 컴포넌트가 순서대로 호출된다")
    void gradeContent_callsComponentsInOrder() {
        // given
        String content = "사업계획서 내용";
        String llmResponse = "{}";

        SpringAiFullReportGradeAgent generator = mock(SpringAiFullReportGradeAgent.class);
        when(generator.generateReport(any())).thenReturn(llmResponse);

        AiReportResponseParser parser = mock(AiReportResponseParser.class);
        when(parser.parse(any())).thenReturn(AiReportResult.fromGradingResult(0, 0, 0, 0, List.of(), List.of(), List.of()));

        SpringAiReportGrader sut = new SpringAiReportGrader(generator, parser);

        // when
        sut.gradeWithSectionAgents(content);

        // then
        var inOrder = inOrder(generator, parser);
        inOrder.verify(generator).generateReport(content);
        inOrder.verify(parser).parse(llmResponse);
    }
}

