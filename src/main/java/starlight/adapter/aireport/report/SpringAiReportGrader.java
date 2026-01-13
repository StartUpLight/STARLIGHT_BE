package starlight.adapter.aireport.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import starlight.adapter.aireport.report.agent.FullReportGradeAgent;
import starlight.adapter.aireport.report.agent.SectionGradeAgent;
import starlight.adapter.aireport.report.dto.SectionGradingResult;
import starlight.adapter.aireport.report.supervisor.SpringAiReportSupervisor;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.ReportGraderPort;
import starlight.application.businessplan.util.BusinessPlanContentExtractor;
import starlight.shared.enumerate.SectionType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 리포트 채점을 오케스트레이션하는 컴포넌트
 * 4개의 섹션별 Advisor를 병렬로 실행하고, 슈퍼바이저가 장단점을 생성
 */
@Slf4j
@Component
public class SpringAiReportGrader implements ReportGraderPort {

    private final Map<SectionType, SectionGradeAgent> sectionGradeAgentMap;
    private final FullReportGradeAgent fullReportGradeAgent;
    private final SpringAiReportSupervisor supervisor;
    private final BusinessPlanContentExtractor contentExtractor;
    private final Executor sectionGradingExecutor;

    public SpringAiReportGrader(
            List<SectionGradeAgent> sectionGradeAgentList,
            FullReportGradeAgent fullReportGradeAgent,
            SpringAiReportSupervisor supervisor,
            BusinessPlanContentExtractor contentExtractor,
            @Qualifier("sectionGradingExecutor") Executor sectionGradingExecutor) {
        this.sectionGradeAgentMap = sectionGradeAgentList.stream()
                .collect(Collectors.toMap(
                        SectionGradeAgent::getSectionType,
                        advisor -> advisor));
        this.fullReportGradeAgent = fullReportGradeAgent;
        this.supervisor = supervisor;
        this.contentExtractor = contentExtractor;
        this.sectionGradingExecutor = sectionGradingExecutor;
    }

    /**
     * PDF에서 추출한 텍스트를 한 번에 채점하는 메소드
     * 전체 프롬프트를 사용하여 LLM에 한 번에 요청하고 결과를 파싱하여 반환
     */
    @Override
    public AiReportResult gradeWithFullPrompt(String content) {
        log.info("전체 프롬프트를 사용한 채점 시작");
        try {
            AiReportResult result = fullReportGradeAgent.gradeFullReport(content);
            log.info("전체 프롬프트를 사용한 채점 완료");
            return result;
        } catch (starlight.domain.aireport.exception.AiReportException e) {
            log.error("전체 프롬프트 채점 중 예외 발생", e);
            throw e;
        }
    }

    /**
     * 섹션별 에이전트를 통해 채점하는 메소드
     * 에이전트 결과를 슈퍼바이저 LLM에 요청하여 결과를 파싱
     */
    @Override
    public AiReportResult gradeWithSectionAgents(Map<SectionType, String> sectionContents, String fullContent) {
        log.info("섹션별 에이전트를 통한 채점 시작");

        if (sectionContents == null || sectionContents.isEmpty()) {
            log.error("섹션별 내용이 비어있습니다");
            throw new starlight.domain.aireport.exception.AiReportException(
                    starlight.domain.aireport.exception.AiReportErrorType.AI_GRADING_FAILED);
        }

        if (fullContent == null || fullContent.trim().isEmpty()) {
            log.error("전체 내용이 비어있습니다");
            throw new starlight.domain.aireport.exception.AiReportException(
                    starlight.domain.aireport.exception.AiReportErrorType.AI_GRADING_FAILED);
        }

        log.debug("섹션별 내용 추출 완료. 섹션 수: {}", sectionContents.size());

        // 4개 섹션을 병렬로 채점
        Map<SectionType, CompletableFuture<SectionGradingResult>> futureMap = Arrays.asList(
                SectionType.PROBLEM_RECOGNITION,
                SectionType.FEASIBILITY,
                SectionType.GROWTH_STRATEGY,
                SectionType.TEAM_COMPETENCE).stream()
                .collect(Collectors.toMap(
                        sectionType -> sectionType,
                        sectionType -> {
                            SectionGradeAgent agent = sectionGradeAgentMap.get(sectionType);
                            String sectionContent = sectionContents.get(sectionType);

                            if (agent != null && sectionContent != null && !sectionContent.isBlank()) {
                                return CompletableFuture
                                        .supplyAsync(
                                                () -> {
                                                    try {
                                                        return agent.gradeSection(sectionContent);
                                                    } catch (Exception e) {
                                                        log.error("[{}] 섹션 채점 중 예외 발생", sectionType, e);
                                                        return SectionGradingResult.failure(sectionType,
                                                                e.getMessage());
                                                    }
                                                },
                                                sectionGradingExecutor)
                                        .exceptionally(ex -> {
                                            log.error("[{}] 섹션 채점 Future 예외 처리", sectionType, ex);
                                            return SectionGradingResult.failure(sectionType, ex.getMessage());
                                        });
                            } else {
                                log.warn("[{}] 섹션 내용이 없거나 Agent가 없습니다. agent={}, content={}",
                                        sectionType, agent != null,
                                        sectionContent != null && !sectionContent.isBlank());
                                return CompletableFuture.completedFuture(
                                        SectionGradingResult.failure(sectionType, "섹션 내용 없음"));
                            }
                        }));

        // 모든 채점 완료 대기 (최대 2분)
        CompletableFuture<?>[] futures = futureMap.values().toArray(new CompletableFuture[0]);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);

        try {
            allFutures.get(2, TimeUnit.MINUTES);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("섹션별 채점 타임아웃 발생. 모든 Future 취소하여 스레드 자원 해제 중...");
            for (CompletableFuture<SectionGradingResult> future : futureMap.values()) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
        } catch (Exception e) {
            log.error("섹션별 채점 중 예외 발생", e);
            for (CompletableFuture<SectionGradingResult> future : futureMap.values()) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
        }

        // 결과 수집
        List<SectionGradingResult> results = futureMap.entrySet().stream()
                .map(entry -> {
                    SectionType sectionType = entry.getKey();
                    CompletableFuture<SectionGradingResult> future = entry.getValue();
                    try {
                        if (future.isCancelled()) {
                            return SectionGradingResult.failure(sectionType, "타임아웃");
                        }
                        return future.get(0, TimeUnit.SECONDS);
                    } catch (java.util.concurrent.TimeoutException e) {
                        return SectionGradingResult.failure(sectionType, "타임아웃");
                    } catch (Exception e) {
                        return SectionGradingResult.failure(sectionType, "예외: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());

        long successCount = results.stream().filter(SectionGradingResult::success).count();
        long failureCount = results.stream().filter(r -> !r.success()).count();
        log.info("모든 섹션 채점 완료. 성공: {}, 실패: {}", successCount, failureCount);

        // 모든 섹션이 실패한 경우 예외 발생
        if (successCount == 0) {
            log.error("모든 섹션 채점이 실패했습니다. 실패 상세: {}",
                    results.stream()
                            .map(r -> String.format("[%s: %s]", r.sectionType(), r.errorMessage()))
                            .collect(Collectors.joining(", ")));
            throw new starlight.domain.aireport.exception.AiReportException(
                    starlight.domain.aireport.exception.AiReportErrorType.AI_GRADING_FAILED);
        }

        // 슈퍼바이저가 장단점 생성
        log.debug("슈퍼바이저 장단점 생성 시작");
        List<AiReportResult.StrengthWeakness> strengths = supervisor.generateStrengths(fullContent, results);
        List<AiReportResult.StrengthWeakness> weaknesses = supervisor.generateWeaknesses(fullContent, results);
        log.debug("슈퍼바이저 장단점 생성 완료. 강점: {}, 약점: {}", strengths.size(), weaknesses.size());

        // 결과 통합
        AiReportResult finalResult = assembleReportResponse(results, strengths, weaknesses);
        log.info("섹션별 채점 최종 완료. 총점: {}, 문제인식={}, 실현가능성={}, 성장전략={}, 팀역량={}",
                finalResult.totalScore(),
                finalResult.problemRecognitionScore(),
                finalResult.feasibilityScore(),
                finalResult.growthStrategyScore(),
                finalResult.teamCompetenceScore());

        return finalResult;
    }

    private AiReportResult assembleReportResponse(
            List<SectionGradingResult> results,
            List<AiReportResult.StrengthWeakness> strengths,
            List<AiReportResult.StrengthWeakness> weaknesses) {
        Integer problemRecognitionScore = extractScore(
                results,
                SectionType.PROBLEM_RECOGNITION);
        Integer feasibilityScore = extractScore(results, SectionType.FEASIBILITY);
        Integer growthStrategyScore = extractScore(results, SectionType.GROWTH_STRATEGY);
        Integer teamCompetenceScore = extractScore(results, SectionType.TEAM_COMPETENCE);

        List<AiReportResult.SectionScoreDetailResponse> sectionScores = results.stream()
                .filter(SectionGradingResult::success)
                .map(SectionGradingResult::sectionScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 기존 AiReportResponse.fromGradingResult 사용 (구조 동일)
        return AiReportResult.fromGradingResult(
                problemRecognitionScore,
                feasibilityScore,
                growthStrategyScore,
                teamCompetenceScore,
                sectionScores,
                strengths,
                weaknesses);
    }

    private Integer extractScore(List<SectionGradingResult> results, SectionType type) {
        return results.stream()
                .filter(r -> r.sectionType() == type)
                .findFirst()
                .map(SectionGradingResult::score)
                .orElse(0);
    }
}
