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
import starlight.application.aireport.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.shared.enumerate.SectionType;

@Slf4j
@RequiredArgsConstructor
public class SpringAiSectionGradeAgent implements SectionGradeAgent {

    private static final int MAX_RETRIES = 3;

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

        Prompt prompt = reportPromptProvider.createSectionGradingPrompt(
                getSectionType(),
                sectionContent);

        ChatClient chatClient = chatClientBuilder.build();
        String filter = buildFilterExpression();
        QuestionAnswerAdvisor qaAdvisor = advisorProvider
                .getQuestionAnswerAdvisor(0.6, 3, filter);
        SimpleLoggerAdvisor slAdvisor = advisorProvider.getSimpleLoggerAdvisor();

        String lastFailureMessage = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            if (attempt > 1) {
                try {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 2s, 4s
                    log.info("[{}] 재시도 대기: {}ms (시도 {}/{})", getSectionType(), delay, attempt, MAX_RETRIES);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                String llmResponse = chatClient
                        .prompt(prompt)
                        .options(ChatOptions.builder()
                                .temperature(0.0)
                                .topP(0.1)
                                .build())
                        .advisors(qaAdvisor, slAdvisor)
                        .call()
                        .content();

                SectionGradingResult result = parseSectionResult(llmResponse);

                if (result.success()) {
                    circuitBreaker.recordSuccess(getSectionType());
                    log.info("[{}] 채점 완료: score={}, filter={}", getSectionType(), result.score(), filter);
                    return result;
                }

                lastFailureMessage = result.errorMessage();
                log.warn("[{}] 채점 실패 (시도 {}/{}): 파싱 결과 유효하지 않음", getSectionType(), attempt, MAX_RETRIES);

            } catch (Exception e) {
                lastFailureMessage = "파싱 실패: " + e.getMessage();
                log.warn("[{}] 채점 실패 (시도 {}/{}): {}", getSectionType(), attempt, MAX_RETRIES, e.getMessage());
            }
        }

        circuitBreaker.recordFailure(getSectionType());
        String errorMessage = lastFailureMessage != null ? lastFailureMessage : "모든 재시도 실패";
        log.error("[{}] 채점 최종 실패 ({}회 시도)", getSectionType(), MAX_RETRIES);
        return SectionGradingResult.failure(getSectionType(), errorMessage);
    }

    private String buildFilterExpression() {
        SectionType sectionType = getSectionType();
        String tag = sectionType.getTag();

        if (tag == null || tag.isBlank()) {
            return null;
        }

        return "tag == '" + tag + "'";
    }

    private SectionGradingResult parseSectionResult(String llmResponse) {
        try {
            // 섹션별 응답 파싱 메소드 사용
            AiReportResult sectionResponse = responseParser.parseSectionResponse(llmResponse);

            // sectionScores에서 해당 섹션 찾기
            String sectionTypeString = getSectionType().name();
            AiReportResult.SectionScoreDetailResponse sectionScore = sectionResponse.sectionScores().stream()
                    .filter(ss -> sectionTypeString.equals(ss.sectionType()))
                    .findFirst()
                    .orElse(null);

            if (sectionScore == null) {
                return SectionGradingResult.failure(
                        getSectionType(),
                        "섹션 점수 누락: sectionScore 없음 (섹션: " + getSectionType() + ")");
            }

            Integer score = getRawScoreForSection(sectionResponse);
            if (score == null) {
                return SectionGradingResult.failure(
                        getSectionType(),
                        "섹션 점수 누락: score 없음 (섹션: " + getSectionType() + ")");
            }

            return SectionGradingResult.success(getSectionType(), score, sectionScore);

        } catch (Exception e) {
            log.error("[{}] 응답 파싱 실패", getSectionType(), e);
            return SectionGradingResult.failure(getSectionType(), "파싱 실패: " + e.getMessage());
        }
    }

    private Integer getRawScoreForSection(AiReportResult result) {
        return switch (getSectionType()) {
            case PROBLEM_RECOGNITION -> result.problemRecognitionScore();
            case FEASIBILITY -> result.feasibilityScore();
            case GROWTH_STRATEGY -> result.growthStrategyScore();
            case TEAM_COMPETENCE -> result.teamCompetenceScore();
            default -> null;
        };
    }
}
