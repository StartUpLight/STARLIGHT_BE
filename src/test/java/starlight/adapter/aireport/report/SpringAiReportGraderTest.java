package starlight.adapter.aireport.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.aireport.report.agent.FullReportGradeAgent;
import starlight.adapter.aireport.report.agent.SectionGradeAgent;
import starlight.adapter.aireport.report.dto.SectionGradingResult;
import starlight.adapter.aireport.report.supervisor.SpringAiReportSupervisor;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.businessplan.util.BusinessPlanContentExtractor;
import starlight.shared.enumerate.SectionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("SpringAiReportGrader 테스트")
class SpringAiReportGraderTest {

    @Test
    @DisplayName("전체 프롬프트로 채점하여 AiReportResult를 반환한다")
    void gradeWithFullPrompt_returnsAiReportResult() {
        // given
        String content = "사업계획서 내용";

        FullReportGradeAgent fullReportGradeAgent = mock(FullReportGradeAgent.class);
        AiReportResult expectedResponse = AiReportResult.fromGradingResult(
                20, 25, 30, 20,
                List.of(new AiReportResult.SectionScoreDetailResponse("PROBLEM_RECOGNITION", "[{\"item\":\"항목1\",\"score\":5,\"maxScore\":5}]")),
                List.of(new AiReportResult.StrengthWeakness("강점1", "내용1")),
                List.of(new AiReportResult.StrengthWeakness("약점1", "내용1"))
        );
        when(fullReportGradeAgent.gradeFullReport(content)).thenReturn(expectedResponse);

        SpringAiReportGrader sut = new SpringAiReportGrader(
                List.of(),
                fullReportGradeAgent,
                mock(SpringAiReportSupervisor.class),
                mock(BusinessPlanContentExtractor.class),
                mock(Executor.class)
        );

        // when
        AiReportResult result = sut.gradeWithFullPrompt(content);

        // then
        assertThat(result).isNotNull();
        assertThat(result.problemRecognitionScore()).isEqualTo(20);
        assertThat(result.feasibilityScore()).isEqualTo(25);
        assertThat(result.growthStrategyScore()).isEqualTo(30);
        assertThat(result.teamCompetenceScore()).isEqualTo(20);
        assertThat(result.totalScore()).isEqualTo(95);
        assertThat(result.strengths()).hasSize(1);
        assertThat(result.weaknesses()).hasSize(1);
        assertThat(result.sectionScores()).hasSize(1);

        verify(fullReportGradeAgent).gradeFullReport(content);
    }

    @Test
    @DisplayName("섹션별 에이전트로 채점하여 AiReportResult를 반환한다")
    void gradeWithSectionAgents_returnsAiReportResult() {
        // given
        Map<SectionType, String> sectionContents = new HashMap<>();
        sectionContents.put(SectionType.PROBLEM_RECOGNITION, "문제인식 내용");
        sectionContents.put(SectionType.FEASIBILITY, "실현가능성 내용");
        sectionContents.put(SectionType.GROWTH_STRATEGY, "성장전략 내용");
        sectionContents.put(SectionType.TEAM_COMPETENCE, "팀역량 내용");
        String fullContent = "전체 사업계획서 내용";

        // 각 섹션에 맞는 Agent 모킹
        SectionGradeAgent problemRecognitionAgent = mock(SectionGradeAgent.class);
        when(problemRecognitionAgent.getSectionType()).thenReturn(SectionType.PROBLEM_RECOGNITION);
        when(problemRecognitionAgent.gradeSection(anyString())).thenReturn(
                SectionGradingResult.success(
                        SectionType.PROBLEM_RECOGNITION,
                        20,
                        new AiReportResult.SectionScoreDetailResponse("PROBLEM_RECOGNITION", "[{\"item\":\"근본 원인 논리 분석\",\"score\":5,\"maxScore\":5}]")
                )
        );

        SectionGradeAgent feasibilityAgent = mock(SectionGradeAgent.class);
        when(feasibilityAgent.getSectionType()).thenReturn(SectionType.FEASIBILITY);
        when(feasibilityAgent.gradeSection(anyString())).thenReturn(
                SectionGradingResult.success(
                        SectionType.FEASIBILITY,
                        25,
                        new AiReportResult.SectionScoreDetailResponse("FEASIBILITY", "[{\"item\":\"로드맵 구체성\",\"score\":6,\"maxScore\":6}]")
                )
        );

        SectionGradeAgent growthStrategyAgent = mock(SectionGradeAgent.class);
        when(growthStrategyAgent.getSectionType()).thenReturn(SectionType.GROWTH_STRATEGY);
        when(growthStrategyAgent.gradeSection(anyString())).thenReturn(
                SectionGradingResult.success(
                        SectionType.GROWTH_STRATEGY,
                        30,
                        new AiReportResult.SectionScoreDetailResponse("GROWTH_STRATEGY", "[{\"item\":\"BM 9요소 완결·연계성\",\"score\":6,\"maxScore\":6}]")
                )
        );

        SectionGradeAgent teamCompetenceAgent = mock(SectionGradeAgent.class);
        when(teamCompetenceAgent.getSectionType()).thenReturn(SectionType.TEAM_COMPETENCE);
        when(teamCompetenceAgent.gradeSection(anyString())).thenReturn(
                SectionGradingResult.success(
                        SectionType.TEAM_COMPETENCE,
                        20,
                        new AiReportResult.SectionScoreDetailResponse("TEAM_COMPETENCE", "[{\"item\":\"창업자 전문성·연관성\",\"score\":5,\"maxScore\":5}]")
                )
        );

        List<SectionGradeAgent> sectionAgents = List.of(
                problemRecognitionAgent,
                feasibilityAgent,
                growthStrategyAgent,
                teamCompetenceAgent
        );

        FullReportGradeAgent fullReportGradeAgent = mock(FullReportGradeAgent.class);
        SpringAiReportSupervisor supervisor = mock(SpringAiReportSupervisor.class);
        BusinessPlanContentExtractor contentExtractor = mock(BusinessPlanContentExtractor.class);
        // 실제 Executor 사용 (비동기 실행을 위해)
        Executor executor = Executors.newFixedThreadPool(4);

        SpringAiReportGrader sut = new SpringAiReportGrader(
                sectionAgents,
                fullReportGradeAgent,
                supervisor,
                contentExtractor,
                executor
        );

        when(supervisor.generateStrengths(anyString(), anyList())).thenReturn(
                List.of(new AiReportResult.StrengthWeakness("강점1", "내용1"))
        );
        when(supervisor.generateWeaknesses(anyString(), anyList())).thenReturn(
                List.of(new AiReportResult.StrengthWeakness("약점1", "내용1"))
        );

        // when
        AiReportResult result = sut.gradeWithSectionAgents(sectionContents, fullContent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.problemRecognitionScore()).isEqualTo(20);
        assertThat(result.feasibilityScore()).isEqualTo(25);
        assertThat(result.growthStrategyScore()).isEqualTo(30);
        assertThat(result.teamCompetenceScore()).isEqualTo(20);
        assertThat(result.totalScore()).isEqualTo(95);
        assertThat(result.strengths()).hasSize(1);
        assertThat(result.weaknesses()).hasSize(1);

        // 각 Agent가 호출되었는지 확인
        verify(problemRecognitionAgent).gradeSection("문제인식 내용");
        verify(feasibilityAgent).gradeSection("실현가능성 내용");
        verify(growthStrategyAgent).gradeSection("성장전략 내용");
        verify(teamCompetenceAgent).gradeSection("팀역량 내용");
        verify(supervisor).generateStrengths(eq(fullContent), anyList());
        verify(supervisor).generateWeaknesses(eq(fullContent), anyList());
    }
}

