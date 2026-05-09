package starlight.adapter.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.aireport.required.AiReportNotificationPort;
import starlight.application.notification.provided.NotificationUseCase;
import starlight.application.notification.provided.dto.input.NotificationSendInput;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSendAdapter implements AiReportNotificationPort {

    private static final String AI_REPORT_COMPLETED = "AI_REPORT_COMPLETED";

    private final NotificationUseCase notificationUseCase;

    @Override
    public void sendAiReportCompleted(Long memberId, Long businessPlanId, String businessPlanTitle) {
        send(NotificationSendInput.of(
                memberId,
                AI_REPORT_COMPLETED,
                "AI 리포트가 완료되었습니다.",
                String.format("'%s' 사업계획서의 AI 리포트를 확인할 수 있습니다.", businessPlanTitle),
                businessPlanId
        ));
    }

    private void send(NotificationSendInput input) {
        try {
            notificationUseCase.notifyMember(input);
        } catch (Exception exception) {
            log.error("[NOTIFICATION] failed to create notification type={}, memberId={}",
                    input.type(), input.memberId(), exception);
        }
    }
}
