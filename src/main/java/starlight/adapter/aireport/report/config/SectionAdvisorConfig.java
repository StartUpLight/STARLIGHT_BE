package starlight.adapter.aireport.reportgrader.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import starlight.adapter.aireport.reportgrader.agent.SectionGradeAgent;
import starlight.adapter.aireport.reportgrader.agent.impl.SpringAiSectionGradeAgent;
import starlight.adapter.aireport.reportgrader.circuitbreaker.SectionGradingCircuitBreaker;
import starlight.adapter.aireport.reportgrader.provider.SpringAiAdvisorProvider;
import starlight.adapter.aireport.reportgrader.provider.ReportPromptProvider;
import starlight.adapter.aireport.reportgrader.util.AiReportResponseParser;
import starlight.shared.enumerate.SectionType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SectionAdvisorConfig {
    
    private final ChatClient.Builder chatClientBuilder;
    private final ReportPromptProvider reportPromptProvider;
    private final SpringAiAdvisorProvider advisorProvider;
    private final AiReportResponseParser responseParser;
    private final SectionGradingCircuitBreaker circuitBreaker;
    
    @Bean
    public List<SectionGradeAgent> sectionAdvisors() {
        // 채점 대상이 아닌 OVERVIEW를 제외한 모든 SectionType에 대해 Advisor 생성
        return Arrays.stream(SectionType.values())
            .filter(sectionType -> sectionType.getTag() != null)  // OVERVIEW 제외
            .map(sectionType -> new SpringAiSectionGradeAgent(
                sectionType,
                chatClientBuilder,
                    reportPromptProvider,
                advisorProvider,
                responseParser,
                circuitBreaker
            ))
            .collect(Collectors.toList());
    }
}

