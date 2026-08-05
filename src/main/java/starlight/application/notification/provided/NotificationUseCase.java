package starlight.application.notification.provided;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.provided.dto.input.NotificationSendInput;
import starlight.application.notification.provided.dto.result.NotificationResult;
import starlight.application.notification.required.dto.NotificationPublishMessage;

import java.util.List;

public interface NotificationUseCase {

    void notifyMember(NotificationSendInput input);

    List<NotificationResult> findAllByMemberId(Long memberId);

    void markAsRead(Long notificationId, Long memberId);

    SseEmitter subscribe(Long memberId, Long lastEventId);

    void sendRealtime(NotificationPublishMessage message);
}
