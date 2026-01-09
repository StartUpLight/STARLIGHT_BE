package starlight.application.aireport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.application.aireport.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.BusinessPlanCreationPort;
import starlight.application.aireport.required.ReportGraderPort;
import starlight.application.aireport.required.AiReportQueryPort;
import starlight.application.aireport.required.AiReportCommandPort;
import starlight.application.aireport.required.OcrProviderPort;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.businessplan.required.BusinessPlanCommandPort;
import starlight.application.businessplan.util.BusinessPlanContentExtractor;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.aireport.exception.AiReportErrorType;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.enumerate.SectionType;
import starlight.shared.valueobject.RawJson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AiReportService 유닛 테스트")
class AiReportServiceUnitTest {

    private final BusinessPlanCommandPort businessPlanCommand = mock(BusinessPlanCommandPort.class);
    private final BusinessPlanQueryPort businessPlanQuery = mock(BusinessPlanQueryPort.class);
    private final BusinessPlanCreationPort businessPlanCreationPort = mock(BusinessPlanCreationPort.class);
    private final AiReportQueryPort aiReportQuery = mock(AiReportQueryPort.class);
    private final AiReportCommandPort aiReportCommand = mock(AiReportCommandPort.class);
    private final ReportGraderPort aiReportGrader = mock(ReportGraderPort.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OcrProviderPort ocrProvider = mock(OcrProviderPort.class);
    private final AiReportResponseParser responseParser = new AiReportResponseParser(objectMapper);
    private final BusinessPlanContentExtractor contentExtractor = mock(BusinessPlanContentExtractor.class);

    private AiReportService sut;

    @Test
    @DisplayName("채점 성공 시 새로운 AiReport를 생성하고 저장한다")
    void gradeBusinessPlan_createsNewReport() {
        // given
        Long planId = 1L;
        Long memberId = 1L;
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.getId()).thenReturn(planId);
        when(plan.isOwnedBy(memberId)).thenReturn(true);
        when(plan.areWritingCompleted()).thenReturn(true);
        when(businessPlanQuery.findByIdOrThrow(planId)).thenReturn(plan);
        when(aiReportQuery.findByBusinessPlanId(planId)).thenReturn(Optional.empty());

        String extractedContent = "사업계획서 내용";
        when(contentExtractor.extractContent(plan)).thenReturn(extractedContent);
        
        Map<SectionType, String> sectionContents = new HashMap<>();
        sectionContents.put(SectionType.PROBLEM_RECOGNITION, "문제인식 내용");
        sectionContents.put(SectionType.FEASIBILITY, "실현가능성 내용");
        sectionContents.put(SectionType.GROWTH_STRATEGY, "성장전략 내용");
        sectionContents.put(SectionType.TEAM_COMPETENCE, "팀역량 내용");
        when(contentExtractor.extractSectionContents(plan)).thenReturn(sectionContents);

        AiReportResult gradingResult = AiReportResult.fromGradingResult(
                20, 25, 30, 20,
                List.of(),
                List.of(),
                List.of()
        );
        when(aiReportGrader.gradeWithSectionAgents(sectionContents, extractedContent)).thenReturn(gradingResult);

        String rawJson = """
                {
                    "problemRecognitionScore": 20,
                    "feasibilityScore": 25,
                    "growthStrategyScore": 30,
                    "teamCompetenceScore": 20,
                    "sectionScores": [],
                    "strengths": [],
                    "weaknesses": []
                }
                """;
        AiReport savedReport = mock(AiReport.class);
        when(savedReport.getId()).thenReturn(1L);
        when(savedReport.getBusinessPlanId()).thenReturn(planId);
        when(savedReport.getRawJson()).thenReturn(RawJson.create(rawJson));
        when(aiReportCommand.save(any(AiReport.class))).thenReturn(savedReport);
        when(businessPlanCommand.save(any(BusinessPlan.class))).thenReturn(plan);

        sut = new AiReportService(businessPlanCommand, businessPlanQuery, businessPlanCreationPort, aiReportQuery, aiReportCommand, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when
        AiReportResult result = sut.gradeBusinessPlan(planId, memberId);

        // then
        assertThat(result).isNotNull();
        verify(plan).updateStatus(PlanStatus.AI_REVIEWED);
        verify(aiReportCommand).save(any(AiReport.class));
    }

    @Test
    @DisplayName("기존 리포트가 있으면 업데이트한다")
    void gradeBusinessPlan_updatesExistingReport() {
        // given
        Long planId = 1L;
        Long memberId = 1L;
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.getId()).thenReturn(planId);
        when(plan.isOwnedBy(memberId)).thenReturn(true);
        when(plan.areWritingCompleted()).thenReturn(true);
        when(businessPlanQuery.findByIdOrThrow(planId)).thenReturn(plan);

        AiReport existingReport = mock(AiReport.class);
        when(aiReportQuery.findByBusinessPlanId(planId)).thenReturn(Optional.of(existingReport));

        String extractedContent = "사업계획서 내용";
        when(contentExtractor.extractContent(plan)).thenReturn(extractedContent);
        
        Map<SectionType, String> sectionContents = new HashMap<>();
        sectionContents.put(SectionType.PROBLEM_RECOGNITION, "문제인식 내용");
        sectionContents.put(SectionType.FEASIBILITY, "실현가능성 내용");
        sectionContents.put(SectionType.GROWTH_STRATEGY, "성장전략 내용");
        sectionContents.put(SectionType.TEAM_COMPETENCE, "팀역량 내용");
        when(contentExtractor.extractSectionContents(plan)).thenReturn(sectionContents);

        AiReportResult gradingResult = AiReportResult.fromGradingResult(
                20, 25, 30, 20,
                List.of(),
                List.of(),
                List.of()
        );
        when(aiReportGrader.gradeWithSectionAgents(sectionContents, extractedContent)).thenReturn(gradingResult);

        String rawJson = """
                {
                    "problemRecognitionScore": 20,
                    "feasibilityScore": 25,
                    "growthStrategyScore": 30,
                    "teamCompetenceScore": 20,
                    "sectionScores": [],
                    "strengths": [],
                    "weaknesses": []
                }
                """;
        when(existingReport.getId()).thenReturn(1L);
        when(existingReport.getBusinessPlanId()).thenReturn(planId);
        when(existingReport.getRawJson()).thenReturn(RawJson.create(rawJson));
        when(aiReportCommand.save(existingReport)).thenReturn(existingReport);
        when(businessPlanCommand.save(any(BusinessPlan.class))).thenReturn(plan);

        sut = new AiReportService(businessPlanCommand, businessPlanQuery, businessPlanCreationPort, aiReportQuery, aiReportCommand, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when
        AiReportResult result = sut.gradeBusinessPlan(planId, memberId);

        // then
        assertThat(result).isNotNull();
        verify(existingReport).update(anyString());
        // 기존 리포트가 있어도 상태는 AI_REVIEWED로 갱신됨
        verify(plan).updateStatus(PlanStatus.AI_REVIEWED);
    }

    @Test
    @DisplayName("소유자가 아니면 예외를 던진다")
    void gradeBusinessPlan_throwsExceptionWhenNotOwner() {
        // given
        Long planId = 1L;
        Long memberId = 1L;
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(memberId)).thenReturn(false);
        when(businessPlanQuery.findByIdOrThrow(planId)).thenReturn(plan);

        sut = new AiReportService(businessPlanCommand, businessPlanQuery, businessPlanCreationPort, aiReportQuery, aiReportCommand, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when & then
        assertThatThrownBy(() -> sut.gradeBusinessPlan(planId, memberId))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.UNAUTHORIZED_ACCESS);
    }

    @Test
    @DisplayName("작성 완료되지 않았으면 예외를 던진다")
    void gradeBusinessPlan_throwsExceptionWhenNotCompleted() {
        // given
        Long planId = 1L;
        Long memberId = 1L;
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(memberId)).thenReturn(true);
        when(plan.areWritingCompleted()).thenReturn(false);
        when(businessPlanQuery.findByIdOrThrow(planId)).thenReturn(plan);

        sut = new AiReportService(businessPlanCommand, businessPlanQuery, businessPlanCreationPort, aiReportQuery, aiReportCommand, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when & then
        assertThatThrownBy(() -> sut.gradeBusinessPlan(planId, memberId))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.NOT_READY_FOR_AI_REPORT);
    }

    @Test
    @DisplayName("리포트 조회 성공 시 AiReportResponse를 반환한다")
    void getAiReport_returnsResponse() {
        // given
        Long planId = 1L;
        Long memberId = 1L;
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.getId()).thenReturn(planId);
        when(plan.isOwnedBy(memberId)).thenReturn(true);
        when(plan.areWritingCompleted()).thenReturn(true);
        when(businessPlanQuery.findByIdOrThrow(planId)).thenReturn(plan);

        String rawJson = """
                {
                    "problemRecognitionScore": 20,
                    "feasibilityScore": 25,
                    "growthStrategyScore": 30,
                    "teamCompetenceScore": 20,
                    "sectionScores": [],
                    "strengths": [],
                    "weaknesses": []
                }
                """;
        AiReport aiReport = mock(AiReport.class);
        when(aiReport.getId()).thenReturn(1L);
        when(aiReport.getBusinessPlanId()).thenReturn(planId);
        when(aiReport.getRawJson()).thenReturn(RawJson.create(rawJson));
        when(aiReportQuery.findByBusinessPlanId(planId)).thenReturn(Optional.of(aiReport));

        sut = new AiReportService(businessPlanCommand, businessPlanQuery, businessPlanCreationPort, aiReportQuery, aiReportCommand, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when
        AiReportResult result = sut.getAiReport(planId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.businessPlanId()).isEqualTo(planId);
    }

    @Test
    @DisplayName("리포트가 없으면 예외를 던진다")
    void getAiReport_throwsExceptionWhenNotFound() {
        // given
        Long planId = 1L;
        Long memberId = 1L;
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.getId()).thenReturn(planId);
        when(plan.isOwnedBy(memberId)).thenReturn(true);
        when(plan.areWritingCompleted()).thenReturn(true);
        when(businessPlanQuery.findByIdOrThrow(planId)).thenReturn(plan);
        when(aiReportQuery.findByBusinessPlanId(planId)).thenReturn(Optional.empty());

        sut = new AiReportService(businessPlanCommand, businessPlanQuery, businessPlanCreationPort, aiReportQuery, aiReportCommand, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when & then
        assertThatThrownBy(() -> sut.getAiReport(planId, memberId))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.AI_REPORT_NOT_FOUND);
    }
}

