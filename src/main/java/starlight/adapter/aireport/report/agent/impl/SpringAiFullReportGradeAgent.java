package starlight.adapter.aireport.report.agent.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import starlight.adapter.aireport.report.agent.FullReportGradeAgent;
import starlight.adapter.aireport.report.provider.SpringAiAdvisorProvider;
import starlight.adapter.aireport.report.provider.ReportPromptProvider;
import starlight.adapter.aireport.report.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.domain.aireport.exception.AiReportErrorType;
import starlight.domain.aireport.exception.AiReportException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiFullReportGradeAgent implements FullReportGradeAgent {

        private final ChatClient.Builder chatClientBuilder;
        private final ReportPromptProvider reportPromptProvider;
        private final SpringAiAdvisorProvider advisorProvider;
        private final AiReportResponseParser responseParser;

        @Override
        public AiReportResult gradeFullReport(String content) {
                if (content == null || content.trim().isEmpty()) {
                        throw new AiReportException(AiReportErrorType.AI_GRADING_FAILED);
                }

                try {
                        Prompt prompt = reportPromptProvider.createReportGradingPrompt(content);

                        ChatClient chatClient = chatClientBuilder.build();
                        QuestionAnswerAdvisor qaAdvisor = advisorProvider
                                        .getQuestionAnswerAdvisor(0.6, 3, null);
                        SimpleLoggerAdvisor slAdvisor = advisorProvider.getSimpleLoggerAdvisor();

                        String llmResponse = chatClient
                                        .prompt(prompt)
                                        .options(ChatOptions.builder()
                                                        .temperature(0.0)
                                                        .topP(0.1)
                                                        .build())
                                        .advisors(qaAdvisor, slAdvisor)
                                        .call()
                                        .content();

                        if (llmResponse == null || llmResponse.trim().isEmpty()) {
                                throw new AiReportException(AiReportErrorType.AI_GRADING_FAILED);
                        }

                        return responseParser.parse(llmResponse);

                } catch (AiReportException e) {
                        throw e;
                } catch (Exception e) {
                        throw new AiReportException(AiReportErrorType.AI_GRADING_FAILED);
                }
        }
}
