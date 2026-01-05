package starlight.adapter.aireport.reportgrader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import starlight.adapter.aireport.reportgrader.agent.SectionGradeAgent;
import starlight.adapter.aireport.reportgrader.dto.SectionGradingResult;
import starlight.adapter.aireport.reportgrader.supervisor.ReportSupervisor;
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
    
    private final Map<SectionType, SectionGradeAgent> advisors;
    private final ReportSupervisor supervisor;
    private final BusinessPlanContentExtractor contentExtractor;
    private final Executor sectionGradingExecutor;
    
    public SpringAiReportGrader(
        List<SectionGradeAgent> advisorList,
        ReportSupervisor supervisor,
        BusinessPlanContentExtractor contentExtractor,
        @Qualifier("sectionGradingExecutor") Executor sectionGradingExecutor
    ) {
        this.advisors = advisorList.stream()
            .collect(Collectors.toMap(
                SectionGradeAgent::getSectionType,
                advisor -> advisor
            ));
        this.supervisor = supervisor;
        this.contentExtractor = contentExtractor;
        this.sectionGradingExecutor = sectionGradingExecutor;
    }
    
    @Override
    public AiReportResult gradeWithSectionAgents(String content) {
        // 섹션별 내용 추출 (전체 텍스트에서)
        Map<SectionType, String> sectionContents = contentExtractor.extractSectionContentsFromText(content);
        
        // 4개 섹션을 병렬로 채점
        List<CompletableFuture<SectionGradingResult>> futures = Arrays.asList(
            SectionType.PROBLEM_RECOGNITION,
            SectionType.FEASIBILITY,
            SectionType.GROWTH_STRATEGY,
            SectionType.TEAM_COMPETENCE
        ).stream()
            .map(sectionType -> {
                SectionGradeAgent advisor = advisors.get(sectionType);
                String sectionContent = sectionContents.get(sectionType);
                
                if (advisor != null && sectionContent != null && !sectionContent.isBlank()) {
                    return CompletableFuture
                        .supplyAsync(
                            () -> advisor.gradeSection(sectionContent),
                            sectionGradingExecutor
                        )
                        .exceptionally(ex -> {
                            log.error("[{}] 채점 중 예외 발생", sectionType, ex);
                            return SectionGradingResult.failure(sectionType, ex.getMessage());
                        });
                } else {
                    // 섹션 내용이 없으면 기본값 반환
                    return CompletableFuture.completedFuture(
                        SectionGradingResult.failure(sectionType, "섹션 내용 없음")
                    );
                }
            })
            .collect(Collectors.toList());
        
        // 모든 채점 완료 대기 (최대 2분)
        List<SectionGradingResult> results = futures.stream()
            .map(future -> {
                try {
                    return future.get(2, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("섹션 채점 Future 완료 실패", e);
                    return SectionGradingResult.failure(
                        SectionType.PROBLEM_RECOGNITION, 
                        "타임아웃 또는 예외"
                    );
                }
            })
            .collect(Collectors.toList());
        
        log.info("모든 섹션 채점 완료. 성공: {}, 실패: {}", 
            results.stream().filter(SectionGradingResult::success).count(),
            results.stream().filter(r -> !r.success()).count()
        );
        
        // 슈퍼바이저가 장단점 생성
        List<AiReportResult.StrengthWeakness> strengths =
            supervisor.generateStrengths(content, results);
        List<AiReportResult.StrengthWeakness> weaknesses =
            supervisor.generateWeaknesses(content, results);
        
        // 결과 통합
        return assembleReportResponse(results, strengths, weaknesses);
    }
    
    private AiReportResult assembleReportResponse(
        List<SectionGradingResult> results,
        List<AiReportResult.StrengthWeakness> strengths,
        List<AiReportResult.StrengthWeakness> weaknesses
    ) {
        Integer problemRecognitionScore = extractScore(
            results, 
            SectionType.PROBLEM_RECOGNITION
        );
        Integer feasibilityScore = extractScore(results, SectionType.FEASIBILITY);
        Integer growthStrategyScore = extractScore(results, SectionType.GROWTH_STRATEGY);
        Integer teamCompetenceScore = extractScore(results, SectionType.TEAM_COMPETENCE);
        
        List<AiReportResult.SectionScoreDetailResponse> sectionScores =
            results.stream()
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
            weaknesses
        );
    }
    
    private Integer extractScore(List<SectionGradingResult> results, SectionType type) {
        return results.stream()
            .filter(r -> r.sectionType() == type)
            .findFirst()
            .map(SectionGradingResult::score)
            .orElse(0);
    }
}
