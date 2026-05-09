package starlight.application.notification.required;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.required.dto.NotificationPublishMessage;

public interface NotificationRealtimePort {

    SseEmitter subscribe(Long memberId);

    void send(NotificationPublishMessage message);
}
