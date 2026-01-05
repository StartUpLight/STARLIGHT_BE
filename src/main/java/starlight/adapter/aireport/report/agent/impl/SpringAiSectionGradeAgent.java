package starlight.adapter.aireport.report.agent.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import starlight.adapter.aireport.report.agent.SectionGradeAgent;
import starlight.adapter.aireport.report.circuitbreaker.SectionGradingCircuitBreaker;
import starlight.adapter.aireport.report.dto.SectionGradingResult;
import starlight.adapter.aireport.report.provider.SpringAiAdvisorProvider;
import starlight.adapter.aireport.report.provider.ReportPromptProvider;
import starlight.adapter.aireport.report.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.shared.enumerate.SectionType;

@Slf4j
@RequiredArgsConstructor
public class SpringAiSectionGradeAgent implements SectionGradeAgent {

    private final SectionType sectionType;
    private final ChatClient.Builder chatClientBuilder;
    private final ReportPromptProvider reportPromptProvider;
    private final SpringAiAdvisorProvider advisorProvider;
    private final AiReportResponseParser responseParser;
    private final SectionGradingCircuitBreaker circuitBreaker;

    @Override
    public SectionType getSectionType() {
        return sectionType;
    }

    @Override
    public SectionGradingResult gradeSection(String sectionContent) {
        // 서킷브레이커 체크
        if (!circuitBreaker.allowRequest(getSectionType())) {
            log.warn("[{}] Circuit breaker is OPEN", getSectionType());
            return SectionGradingResult.failure(getSectionType(), "Circuit breaker is OPEN");
        }

        try {
            Prompt prompt = reportPromptProvider.createSectionGradingPrompt(
                    getSectionType(),
                    sectionContent);

            ChatClient chatClient = chatClientBuilder.build();

            // SectionType의 tag만 사용
            String filter = buildFilterExpression();
            QuestionAnswerAdvisor qaAdvisor = advisorProvider
                    .getQuestionAnswerAdvisor(0.6, 3, filter);
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

            // 섹션별 응답 파싱
            SectionGradingResult result = parseSectionResult(llmResponse);

            if (result.success()) {
                circuitBreaker.recordSuccess(getSectionType());
            } else {
                circuitBreaker.recordFailure(getSectionType());
            }

            log.info("[{}] 채점 완료: score={}, filter={}",
                    getSectionType(), result.score(), filter);
            return result;

        } catch (Exception e) {
            circuitBreaker.recordFailure(getSectionType());
            log.error("[{}] 채점 실패", getSectionType(), e);
            return SectionGradingResult.failure(getSectionType(), e.getMessage());
        }
    }

    private SectionGradingResult parseSectionResult(String llmResponse) {
        try {
            // 섹션별 응답 파싱 메소드 사용
            AiReportResult sectionResponse = responseParser.parseSectionResponse(llmResponse);

            // SectionType의 score 추출 메서드 사용
            Integer score = getSectionType().extractScore(sectionResponse);

            // sectionScores에서 해당 섹션 찾기
            String sectionTypeString = getSectionType().getSectionTypeString();
            AiReportResult.SectionScoreDetailResponse sectionScore = null;
            if (sectionTypeString != null) {
                sectionScore = sectionResponse.sectionScores().stream()
                        .filter(ss -> sectionTypeString.equals(ss.sectionType()))
                        .findFirst()
                        .orElse(null);
            }

            return SectionGradingResult.success(getSectionType(), score, sectionScore);

        } catch (Exception e) {
            log.error("[{}] 응답 파싱 실패", getSectionType(), e);
            return SectionGradingResult.failure(getSectionType(), "파싱 실패: " + e.getMessage());
        }
    }
}
