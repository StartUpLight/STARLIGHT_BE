package starlight.adapter.notification;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import starlight.application.notification.provided.NotificationUseCase;
import starlight.application.notification.provided.dto.input.NotificationSendInput;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationSendAdapterTest {

    private final NotificationUseCase notificationUseCase = mock(NotificationUseCase.class);
    private final NotificationSendAdapter adapter = new NotificationSendAdapter(notificationUseCase);

    @Test
    void sendAiReportCompleted_알림생성을_위임한다() {
        adapter.sendAiReportCompleted(1L, 10L, "사업계획서");

        NotificationSendInput input = captureNotificationSendInput();
        assertThat(input.memberId()).isEqualTo(1L);
        assertThat(input.type()).isEqualTo("AI_REPORT_COMPLETED");
        assertThat(input.referenceId()).isEqualTo(10L);
        assertThat(input.title()).isEqualTo("AI 리포트가 완료되었습니다.");
    }

    @Test
    void sendExpertReportSubmitted_알림생성을_위임한다() {
        adapter.sendExpertReportSubmitted(1L, 10L, "사업계획서");

        NotificationSendInput input = captureNotificationSendInput();
        assertThat(input.memberId()).isEqualTo(1L);
        assertThat(input.type()).isEqualTo("EXPERT_REPORT_SUBMITTED");
        assertThat(input.referenceId()).isEqualTo(10L);
        assertThat(input.title()).isEqualTo("전문가 피드백이 도착했습니다.");
    }

    @Test
    void sendPaymentCompleted_알림생성을_위임한다() {
        adapter.sendPaymentCompleted(1L, 20L, "AI 리포트 1회권", 1);

        NotificationSendInput input = captureNotificationSendInput();
        assertThat(input.memberId()).isEqualTo(1L);
        assertThat(input.type()).isEqualTo("PAYMENT_COMPLETED");
        assertThat(input.referenceId()).isEqualTo(20L);
        assertThat(input.title()).isEqualTo("결제가 완료되었습니다.");
    }

    @Test
    void 알림생성실패는_호출자에게_전파하지_않는다() {
        doThrow(new RuntimeException("notification failed")).when(notificationUseCase).notifyMember(any());

        assertThatCode(() -> adapter.sendPaymentCompleted(1L, 20L, "AI 리포트 1회권", 1))
                .doesNotThrowAnyException();
    }

    private NotificationSendInput captureNotificationSendInput() {
        ArgumentCaptor<NotificationSendInput> captor = ArgumentCaptor.forClass(NotificationSendInput.class);
        verify(notificationUseCase).notifyMember(captor.capture());
        return captor.getValue();
    }
}
