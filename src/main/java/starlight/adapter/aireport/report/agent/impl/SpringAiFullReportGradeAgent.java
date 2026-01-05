package starlight.adapter.aireport.report.agent.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import starlight.adapter.aireport.report.provider.SpringAiAdvisorProvider;
import starlight.adapter.aireport.report.provider.ReportPromptProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiGenerator {

    private final ChatClient.Builder chatClientBuilder;
    private final ReportPromptProvider reportPromptProvider;
    private final SpringAiAdvisorProvider advisorProvider;

    /**
     * 전체 프롬프트를 사용하여 LLM에 리포트 채점을 요청하고 응답을 반환
     * @param content PDF에서 추출한 텍스트 또는 전체 사업계획서 내용
     * @return LLM 응답 문자열 (JSON 형식)
     */
    public String generateReport(String content) {
        Prompt prompt = reportPromptProvider.createReportGradingPrompt(content);

        ChatClient chatClient = chatClientBuilder.build();
        QuestionAnswerAdvisor qaAdvisor = advisorProvider
                .getQuestionAnswerAdvisor(0.6, 3, null);
        SimpleLoggerAdvisor slAdvisor = advisorProvider.getSimpleLoggerAdvisor();

        return chatClient
                .prompt(prompt)
                .options(ChatOptions.builder()
                        .temperature(0.0)
                        .topP(0.1)
                        .build())
                .advisors(qaAdvisor, slAdvisor)
                .call()
                .content();
    }
}
