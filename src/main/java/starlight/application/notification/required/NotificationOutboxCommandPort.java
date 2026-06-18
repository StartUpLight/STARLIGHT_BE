package starlight.application.notification.required;

import starlight.domain.notification.entity.NotificationOutbox;

public interface NotificationOutboxCommandPort {

    NotificationOutbox save(NotificationOutbox outbox);
}
