package starlight.application.notification.required;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.required.dto.NotificationPublishMessage;

import java.util.List;

public interface NotificationRealtimePort {

    SseEmitter subscribe(Long memberId, List<NotificationPublishMessage> missedMessages);

    void send(NotificationPublishMessage message);
}
