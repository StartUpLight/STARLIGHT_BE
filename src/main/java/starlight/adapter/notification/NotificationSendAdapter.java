package starlight.adapter.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.aireport.required.AiReportNotificationPort;
import starlight.application.expertReport.required.ExpertReportNotificationPort;
import starlight.application.notification.provided.NotificationUseCase;
import starlight.application.notification.provided.dto.input.NotificationSendInput;
import starlight.application.order.required.OrderNotificationPort;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSendAdapter implements AiReportNotificationPort,
        ExpertReportNotificationPort,
        OrderNotificationPort {

    private static final String AI_REPORT_COMPLETED = "AI_REPORT_COMPLETED";
    private static final String EXPERT_REPORT_SUBMITTED = "EXPERT_REPORT_SUBMITTED";
    private static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";

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

    @Override
    public void sendExpertReportSubmitted(Long memberId, Long businessPlanId, String businessPlanTitle) {
        send(NotificationSendInput.of(
                memberId,
                EXPERT_REPORT_SUBMITTED,
                "전문가 피드백이 도착했습니다.",
                String.format("'%s' 사업계획서에 대한 전문가 피드백이 제출되었습니다.", businessPlanTitle),
                businessPlanId
        ));
    }

    @Override
    public void sendPaymentCompleted(Long memberId, Long orderId, String productName, int usageCount) {
        send(NotificationSendInput.of(
                memberId,
                PAYMENT_COMPLETED,
                "결제가 완료되었습니다.",
                String.format("%s 결제가 완료되어 이용권 %d회가 충전되었습니다.", productName, usageCount),
                orderId
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
