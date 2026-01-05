package starlight.adapter.aireport.reportgrader.supervisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import starlight.adapter.aireport.reportgrader.dto.SectionGradingResult;
import starlight.adapter.aireport.reportgrader.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportSupervisor {
    
    private final ChatClient.Builder chatClientBuilder;
    private final AiReportResponseParser responseParser;
    
    @Value("${prompt.report.supervisor.system}")
    private String supervisorSystemPrompt;
    
    @Value("${prompt.report.supervisor.user.template}")
    private String supervisorUserPromptTemplate;
    
    public List<AiReportResult.StrengthWeakness> generateStrengths(
        String businessPlanContent,
        List<SectionGradingResult> sectionResults
    ) {
        return generateStrengthWeakness(businessPlanContent, sectionResults, "strengths");
    }
    
    public List<AiReportResult.StrengthWeakness> generateWeaknesses(
        String businessPlanContent,
        List<SectionGradingResult> sectionResults
    ) {
        return generateStrengthWeakness(businessPlanContent, sectionResults, "weaknesses");
    }
    
    private List<AiReportResult.StrengthWeakness> generateStrengthWeakness(
        String businessPlanContent,
        List<SectionGradingResult> sectionResults,
        String type
    ) {
        try {
            String prompt = buildSupervisorPrompt(businessPlanContent, sectionResults, type);
            
            ChatClient chatClient = chatClientBuilder.build();
            String llmResponse = chatClient
                .prompt(new Prompt(List.of(
                    new SystemMessage(supervisorSystemPrompt),
                    new UserMessage(prompt)
                )))
                .options(ChatOptions.builder()
                    .temperature(0.1)
                    .topP(0.2)
                    .build())
                .call()
                .content();
            
            return responseParser.parseStrengthWeakness(llmResponse, type);
            
        } catch (Exception e) {
            log.error("Supervisor failed to generate {}", type, e);
            return List.of();  // 빈 리스트 반환
        }
    }
    
    private String buildSupervisorPrompt(
        String businessPlanContent,
        List<SectionGradingResult> sectionResults,
        String type
    ) {
        // 섹션별 채점 결과 포맷팅
        StringBuilder sectionResultsBuilder = new StringBuilder();
        for (SectionGradingResult result : sectionResults) {
            if (result.success()) {
                sectionResultsBuilder.append(String.format("- %s: %d점\n", 
                    result.sectionType().getDescription(), 
                    result.score()));
            } else {
                sectionResultsBuilder.append(String.format("- %s: 채점 실패 (%s)\n", 
                    result.sectionType().getDescription(), 
                    result.errorMessage()));
            }
        }
        
        PromptTemplate promptTemplate = new PromptTemplate(supervisorUserPromptTemplate);
        Map<String, Object> variables = new HashMap<>();
        variables.put("businessPlanContent", businessPlanContent);
        variables.put("sectionResults", sectionResultsBuilder.toString().trim());
        variables.put("type", type);
        
        return promptTemplate.render(variables);
    }
}

