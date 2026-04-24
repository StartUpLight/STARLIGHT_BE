package starlight.application.notification.provided;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.provided.dto.input.NotificationSendInput;
import starlight.application.notification.provided.dto.result.NotificationResult;

import java.util.List;

public interface NotificationUseCase {

    void notifyMember(NotificationSendInput input);

    List<NotificationResult> findAllByMemberId(Long memberId);

    void markAsRead(Long notificationId, Long memberId);

    SseEmitter subscribe(Long memberId);
}
