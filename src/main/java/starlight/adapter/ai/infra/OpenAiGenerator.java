package starlight.adapter.ai.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import starlight.application.infrastructure.provided.LlmGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiGenerator implements LlmGenerator {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptProvider promptProvider;
    private final AdvisorProvider advisorProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Boolean> generateChecklistArray(
            SubSectionType subSectionType,
            String content,
            List<String> criteria,
            List<String> detailedCriteria
    ) {
        Prompt prompt = promptProvider.createChecklistGradingPrompt(
                subSectionType, content, criteria, detailedCriteria
        );

        ChatClient chatClient = chatClientBuilder.build();

        SimpleLoggerAdvisor slAdvisor = advisorProvider.getSimpleLoggerAdvisor();

        String output = chatClient
                .prompt(prompt)
                .options(ChatOptions.builder()
                        .temperature(0.1)
                        .topP(0.1)
                        .build())
                .advisors(slAdvisor)
                .call()
                .content();

        try {
            return objectMapper.readValue(output, new TypeReference<List<Boolean>>() {
            });
        } catch (Exception e) {
            return List.of(false, false, false, false, false);
        }
    }

    @Override
    public String generateReport(String content) {
        Prompt prompt = promptProvider.createReportGradingPrompt(content);

        ChatClient chatClient = chatClientBuilder.build();
        QuestionAnswerAdvisor qaAdvisor = advisorProvider
                .getQuestionAnswerAdvisor(0.6, 3, null);
        SimpleLoggerAdvisor slAdvisor = advisorProvider.getSimpleLoggerAdvisor();

        return chatClient
                .prompt(prompt)
                .advisors(qaAdvisor, slAdvisor)
                .call()
                .content();
    }
}
