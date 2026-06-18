package starlight.application.notification.required;

import starlight.domain.notification.entity.Notification;

public interface NotificationCommandPort {

    Notification save(Notification notification);
}
