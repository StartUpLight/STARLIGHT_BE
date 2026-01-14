package starlight.adapter.businessplan.checklist.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import starlight.adapter.businessplan.checklist.provider.ChecklistPromptProvider;
import starlight.adapter.aireport.report.provider.SpringAiAdvisorProvider;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiChecklistAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final ChecklistPromptProvider checklistPromptProvider;
    private final SpringAiAdvisorProvider advisorProvider;
    private final ObjectMapper objectMapper;

    public List<Boolean> generateChecklistArray(
            SubSectionType subSectionType,
            String content,
            List<String> criteria,
            List<String> detailedCriteria
    ) {
        Prompt prompt = checklistPromptProvider.createChecklistGradingPrompt(
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
}
