package starlight.adapter.ai.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import starlight.adapter.aireport.report.provider.SpringAiAdvisorProvider;
import starlight.adapter.aireport.report.agent.impl.SpringAiFullReportGradeAgent;
import starlight.adapter.aireport.report.provider.ReportPromptProvider;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

class OpenAiGeneratorTest {

    @Test
    @DisplayName("올바른 JSON 배열을 파싱해 반환")
    void generateChecklistArray_parsesJson() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        // RETURNS_DEEP_STUBS를 사용하면 체인 전체가 자동으로 mock됨
        // 마지막 content()만 반환값 설정
        when(chatClient.prompt(any(Prompt.class))
                .options(any())
                .advisors(any(org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.class))
                .call()
                .content()).thenReturn("[true,false,true,false,true]");

        ReportPromptProvider reportPromptProvider = mock(ReportPromptProvider.class);
        when(reportPromptProvider.createChecklistGradingPrompt(any(SubSectionType.class), anyString(), anyList(), anyList()))
                .thenReturn(mock(Prompt.class));

        SpringAiAdvisorProvider advisorProvider = mock(SpringAiAdvisorProvider.class);
        when(advisorProvider.getSimpleLoggerAdvisor())
                .thenReturn(mock(org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.class));

        SpringAiFullReportGradeAgent sut = new SpringAiFullReportGradeAgent(builder, reportPromptProvider, advisorProvider);

        List<Boolean> result = sut.generateChecklistArray(
                SubSectionType.OVERVIEW_BASIC,
                "test content",
                List.of("c1", "c2", "c3", "c4", "c5"),
                List.of("d1", "d2", "d3", "d4", "d5")
        );
        assertThat(result).containsExactly(true, false, true, false, true);
    }

    @Test
    @DisplayName("파싱 실패 시 보수적으로 모두 false 반환")
    void generateChecklistArray_parseFail_returnsAllFalse() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        // RETURNS_DEEP_STUBS를 사용하면 체인 전체가 자동으로 mock됨
        // 마지막 content()만 반환값 설정
        when(chatClient.prompt(any(Prompt.class))
                .options(any())
                .advisors(any(org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.class))
                .call()
                .content()).thenReturn("not-json");

        ReportPromptProvider reportPromptProvider = mock(ReportPromptProvider.class);
        when(reportPromptProvider.createChecklistGradingPrompt(any(SubSectionType.class), anyString(), anyList(), anyList()))
                .thenReturn(mock(Prompt.class));

        SpringAiAdvisorProvider advisorProvider = mock(SpringAiAdvisorProvider.class);
        when(advisorProvider.getSimpleLoggerAdvisor())
                .thenReturn(mock(org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.class));

        SpringAiFullReportGradeAgent sut = new SpringAiFullReportGradeAgent(builder, reportPromptProvider, advisorProvider);

        List<Boolean> result = sut.generateChecklistArray(
                SubSectionType.OVERVIEW_BASIC,
                "test content",
                List.of("c1", "c2", "c3", "c4", "c5"),
                List.of("d1", "d2", "d3", "d4", "d5")
        );
        assertThat(result).containsExactly(false, false, false, false, false);
    }

    @Test
    @DisplayName("generateReport는 OpenAI 응답 문자열을 반환한다")
    void generateReport_returnsString() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        String expectedResponse = """
                {
                    "problemRecognitionScore": 20,
                    "feasibilityScore": 25,
                    "growthStrategyScore": 30,
                    "teamCompetenceScore": 20,
                    "sectionScores": [],
                    "strengths": [],
                    "weaknesses": []
                }
                """.trim();

        // RETURNS_DEEP_STUBS를 사용하면 체인 전체가 자동으로 mock됨
        // 마지막 content()만 반환값 설정
        when(chatClient.prompt(any(Prompt.class))
                .options(any())
                .advisors(any(), any())
                .call()
                .content()).thenReturn(expectedResponse);

        ReportPromptProvider reportPromptProvider = mock(ReportPromptProvider.class);
        when(reportPromptProvider.createReportGradingPrompt(anyString()))
                .thenReturn(mock(Prompt.class));

        SpringAiAdvisorProvider advisorProvider = mock(SpringAiAdvisorProvider.class);
        when(advisorProvider.getQuestionAnswerAdvisor(anyDouble(), anyInt(), any()))
                .thenReturn(mock(org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.class));
        when(advisorProvider.getSimpleLoggerAdvisor())
                .thenReturn(mock(org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.class));

        SpringAiFullReportGradeAgent sut = new SpringAiFullReportGradeAgent(builder, reportPromptProvider, advisorProvider);

        String result = sut.generateReport("test content");

        assertThat(result).isEqualTo(expectedResponse);
    }
}
