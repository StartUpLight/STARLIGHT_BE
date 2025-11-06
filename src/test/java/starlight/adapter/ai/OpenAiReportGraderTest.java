package starlight.adapter.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.ai.infra.OpenAiGenerator;
import starlight.adapter.ai.util.AiReportResponseParser;
import starlight.adapter.ai.util.BusinessPlanContentExtractor;
import starlight.application.aireport.dto.AiReportResponse;
import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("OpenAiReportGrader 테스트")
class OpenAiReportGraderTest {

    @Test
    @DisplayName("BusinessPlan을 채점하여 AiReportResponse를 반환한다")
    void grade_returnsAiReportResponse() {
        // given
        BusinessPlan businessPlan = mock(BusinessPlan.class);
        String extractedContent = "사업계획서 내용";
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

        BusinessPlanContentExtractor contentExtractor = mock(BusinessPlanContentExtractor.class);
        when(contentExtractor.extractContent(businessPlan)).thenReturn(extractedContent);

        OpenAiGenerator generator = mock(OpenAiGenerator.class);
        when(generator.generateReport(extractedContent)).thenReturn(llmResponse);

        AiReportResponseParser parser = mock(AiReportResponseParser.class);
        AiReportResponse expectedResponse = AiReportResponse.fromGradingResult(
                20, 25, 30, 20,
                List.of(new AiReportResponse.SectionScoreDetailResponse("PROBLEM_RECOGNITION", "[{\"item\":\"항목1\",\"score\":5,\"maxScore\":5}]")),
                List.of(new AiReportResponse.StrengthWeakness("강점1", "내용1")),
                List.of(new AiReportResponse.StrengthWeakness("약점1", "내용1"))
        );
        when(parser.parse(llmResponse)).thenReturn(expectedResponse);

        OpenAiReportGrader sut = new OpenAiReportGrader(generator, contentExtractor, parser);

        // when
        AiReportResponse result = sut.grade(businessPlan);

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

        verify(contentExtractor).extractContent(businessPlan);
        verify(generator).generateReport(extractedContent);
        verify(parser).parse(llmResponse);
    }

    @Test
    @DisplayName("각 컴포넌트가 순서대로 호출된다")
    void grade_callsComponentsInOrder() {
        // given
        BusinessPlan businessPlan = mock(BusinessPlan.class);
        String extractedContent = "사업계획서 내용";
        String llmResponse = "{}";

        BusinessPlanContentExtractor contentExtractor = mock(BusinessPlanContentExtractor.class);
        when(contentExtractor.extractContent(any())).thenReturn(extractedContent);

        OpenAiGenerator generator = mock(OpenAiGenerator.class);
        when(generator.generateReport(any())).thenReturn(llmResponse);

        AiReportResponseParser parser = mock(AiReportResponseParser.class);
        when(parser.parse(any())).thenReturn(AiReportResponse.fromGradingResult(0, 0, 0, 0, List.of(), List.of(), List.of()));

        OpenAiReportGrader sut = new OpenAiReportGrader(generator, contentExtractor, parser);

        // when
        sut.grade(businessPlan);

        // then
        var inOrder = inOrder(contentExtractor, generator, parser);
        inOrder.verify(contentExtractor).extractContent(businessPlan);
        inOrder.verify(generator).generateReport(extractedContent);
        inOrder.verify(parser).parse(llmResponse);
    }
}

