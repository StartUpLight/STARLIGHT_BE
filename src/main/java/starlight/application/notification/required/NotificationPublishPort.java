package starlight.application.notification.required;

import starlight.application.notification.required.dto.NotificationPublishMessage;

public interface NotificationPublishPort {

    void publish(NotificationPublishMessage message);
}
