package starlight.application.aireport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.ai.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResponse;
import starlight.application.aireport.required.AiReportGrader;
import starlight.application.aireport.required.AiReportQuery;
import starlight.application.aireport.required.OcrProvider;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.util.BusinessPlanContentExtractor;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.aireport.exception.AiReportErrorType;
import starlight.domain.aireport.exception.AiReportException;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.valueobject.RawJson;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AiReportServiceImpl 유닛 테스트")
class AiReportServiceImplUnitTest {

    private final BusinessPlanQuery businessPlanQuery = mock(BusinessPlanQuery.class);
    private final BusinessPlanService businessPlanService = mock(BusinessPlanService.class);
    private final AiReportQuery aiReportQuery = mock(AiReportQuery.class);
    private final AiReportGrader aiReportGrader = mock(AiReportGrader.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OcrProvider ocrProvider = mock(OcrProvider.class);
    private final AiReportResponseParser responseParser = new AiReportResponseParser(objectMapper);
    private final BusinessPlanContentExtractor contentExtractor = mock(BusinessPlanContentExtractor.class);

    private AiReportServiceImpl sut;

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
        when(businessPlanQuery.getOrThrow(planId)).thenReturn(plan);
        when(aiReportQuery.findByBusinessPlanId(planId)).thenReturn(Optional.empty());

        String extractedContent = "사업계획서 내용";
        when(contentExtractor.extractContent(plan)).thenReturn(extractedContent);

        AiReportResponse gradingResult = AiReportResponse.fromGradingResult(
                20, 25, 30, 20,
                List.of(),
                List.of(),
                List.of()
        );
        when(aiReportGrader.gradeContent(extractedContent)).thenReturn(gradingResult);

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
        when(aiReportQuery.save(any(AiReport.class))).thenReturn(savedReport);

        sut = new AiReportServiceImpl(businessPlanQuery, businessPlanService, aiReportQuery, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when
        AiReportResponse result = sut.gradeBusinessPlan(planId, memberId);

        // then
        assertThat(result).isNotNull();
        verify(plan).updateStatus(PlanStatus.AI_REVIEWED);
        verify(aiReportQuery).save(any(AiReport.class));
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
        when(businessPlanQuery.getOrThrow(planId)).thenReturn(plan);

        AiReport existingReport = mock(AiReport.class);
        when(aiReportQuery.findByBusinessPlanId(planId)).thenReturn(Optional.of(existingReport));

        String extractedContent = "사업계획서 내용";
        when(contentExtractor.extractContent(plan)).thenReturn(extractedContent);

        AiReportResponse gradingResult = AiReportResponse.fromGradingResult(
                20, 25, 30, 20,
                List.of(),
                List.of(),
                List.of()
        );
        when(aiReportGrader.gradeContent(extractedContent)).thenReturn(gradingResult);

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
        when(aiReportQuery.save(existingReport)).thenReturn(existingReport);

        sut = new AiReportServiceImpl(businessPlanQuery, businessPlanService, aiReportQuery, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when
        AiReportResponse result = sut.gradeBusinessPlan(planId, memberId);

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
        when(businessPlanQuery.getOrThrow(planId)).thenReturn(plan);

        sut = new AiReportServiceImpl(businessPlanQuery, businessPlanService, aiReportQuery, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

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
        when(businessPlanQuery.getOrThrow(planId)).thenReturn(plan);

        sut = new AiReportServiceImpl(businessPlanQuery, businessPlanService, aiReportQuery, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

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
        when(businessPlanQuery.getOrThrow(planId)).thenReturn(plan);

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

        sut = new AiReportServiceImpl(businessPlanQuery, businessPlanService, aiReportQuery, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when
        AiReportResponse result = sut.getAiReport(planId, memberId);

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
        when(businessPlanQuery.getOrThrow(planId)).thenReturn(plan);
        when(aiReportQuery.findByBusinessPlanId(planId)).thenReturn(Optional.empty());

        sut = new AiReportServiceImpl(businessPlanQuery, businessPlanService, aiReportQuery, aiReportGrader, objectMapper, ocrProvider, responseParser, contentExtractor);

        // when & then
        assertThatThrownBy(() -> sut.getAiReport(planId, memberId))
                .isInstanceOf(AiReportException.class)
                .extracting("errorType")
                .isEqualTo(AiReportErrorType.AI_REPORT_NOT_FOUND);
    }
}

