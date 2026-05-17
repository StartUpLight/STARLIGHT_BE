package starlight.application.notification.required;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.required.dto.NotificationRealtimeMessage;

import java.util.List;

public interface NotificationRealtimePort {

    SseEmitter subscribe(Long memberId, List<NotificationRealtimeMessage> missedMessages);

    boolean hasSubscriber(Long memberId);

    void send(NotificationRealtimeMessage message);
}
