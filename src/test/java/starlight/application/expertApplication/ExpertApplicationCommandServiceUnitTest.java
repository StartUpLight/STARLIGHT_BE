package starlight.application.expertApplication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.expertApplication.event.FeedbackRequestInput;
import starlight.application.expertApplication.required.ExpertApplicationQueryPort;
import starlight.application.expertApplication.required.ExpertLookupPort;
import starlight.application.expertReport.provided.ExpertReportUseCase;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expertApplication.entity.ExpertApplication;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertApplicationCommandServiceUnitTest {

    private static final Long EXPERT_ID = 1L;
    private static final Long PLAN_ID = 10L;
    private static final String MENTEE_NAME = "멘티";
    private static final String FEEDBACK_URL = "https://starlight.example.com/feedback/token";

    @Mock
    private ExpertLookupPort expertLookupPort;

    @Mock
    private BusinessPlanQueryPort planQuery;

    @Mock
    private ExpertApplicationQueryPort applicationQueryPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ExpertReportUseCase expertReportUseCase;

    @InjectMocks
    private ExpertApplicationCommandService sut;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sut, "FEEDBACK_DEADLINE_DAYS", 7L);
    }

    @Test
    @DisplayName("피드백 요청 시 전달된 pdfUrl을 사업계획서 보기 URL로 사용한다")
    void requestFeedbackUsesProvidedPdfUrl() {
        BusinessPlan plan = pdfPlan("https://storage.example.com/fallback.pdf");
        Expert expert = expert();
        String requestedPdfUrl = "https://storage.example.com/requested.pdf";

        givenFeedbackRequestContext(plan, expert);

        sut.requestFeedback(EXPERT_ID, PLAN_ID, requestedPdfUrl, MENTEE_NAME);

        FeedbackRequestInput event = captureFeedbackRequestEvent();
        assertThat(event.planFileUrl()).isEqualTo(requestedPdfUrl);
        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.EXPERT_MATCHED);
    }

    @Test
    @DisplayName("pdfUrl이 없으면 PDF 기반 사업계획서의 기존 pdfUrl을 사용한다")
    void requestFeedbackFallsBackToBusinessPlanPdfUrl() {
        String planPdfUrl = "https://storage.example.com/plan.pdf";
        BusinessPlan plan = pdfPlan(planPdfUrl);
        Expert expert = expert();

        givenFeedbackRequestContext(plan, expert);

        sut.requestFeedback(EXPERT_ID, PLAN_ID, null, MENTEE_NAME);

        FeedbackRequestInput event = captureFeedbackRequestEvent();
        assertThat(event.planFileUrl()).isEqualTo(planPdfUrl);
    }

    @Test
    @DisplayName("사용 가능한 PDF URL이 없으면 INVALID_PDF_URL 예외가 발생한다")
    void requestFeedbackThrowsWhenPdfUrlCannotBeResolved() {
        BusinessPlan plan = BusinessPlan.create("사업계획서", 100L);
        ReflectionTestUtils.setField(plan, "id", PLAN_ID);
        when(planQuery.findByIdOrThrow(PLAN_ID)).thenReturn(plan);

        assertThatThrownBy(() -> sut.requestFeedback(EXPERT_ID, PLAN_ID, null, MENTEE_NAME))
                .isInstanceOf(ExpertApplicationException.class)
                .extracting("errorType")
                .isEqualTo(ExpertApplicationErrorType.INVALID_PDF_URL);

        verify(expertLookupPort, never()).findByIdOrThrow(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("https가 아닌 PDF URL은 INVALID_PDF_URL 예외가 발생한다")
    void requestFeedbackThrowsWhenPdfUrlIsNotHttps() {
        BusinessPlan plan = pdfPlan("https://storage.example.com/plan.pdf");
        when(planQuery.findByIdOrThrow(PLAN_ID)).thenReturn(plan);

        assertThatThrownBy(() -> sut.requestFeedback(
                EXPERT_ID, PLAN_ID, "http://storage.example.com/plan.pdf", MENTEE_NAME
        ))
                .isInstanceOf(ExpertApplicationException.class)
                .extracting("errorType")
                .isEqualTo(ExpertApplicationErrorType.INVALID_PDF_URL);

        verify(expertLookupPort, never()).findByIdOrThrow(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private void givenFeedbackRequestContext(BusinessPlan plan, Expert expert) {
        when(planQuery.findByIdOrThrow(PLAN_ID)).thenReturn(plan);
        when(expertLookupPort.findByIdOrThrow(EXPERT_ID)).thenReturn(expert);
        when(applicationQueryPort.existsByExpertIdAndBusinessPlanId(EXPERT_ID, PLAN_ID)).thenReturn(false);
        when(applicationQueryPort.save(any(ExpertApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expertReportUseCase.createExpertReportLink(EXPERT_ID, PLAN_ID)).thenReturn(FEEDBACK_URL);
    }

    private FeedbackRequestInput captureFeedbackRequestEvent() {
        ArgumentCaptor<FeedbackRequestInput> captor = ArgumentCaptor.forClass(FeedbackRequestInput.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    private BusinessPlan pdfPlan(String pdfUrl) {
        BusinessPlan plan = BusinessPlan.createWithPdf("사업계획서", 100L, pdfUrl);
        ReflectionTestUtils.setField(plan, "id", PLAN_ID);
        return plan;
    }

    private Expert expert() {
        Expert expert = Expert.createBackoffice("멘토", "mentor@example.com", "소개", List.of(), List.of());
        ReflectionTestUtils.setField(expert, "id", EXPERT_ID);
        return expert;
    }
}
