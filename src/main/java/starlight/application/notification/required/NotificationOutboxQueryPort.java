package starlight.application.notification.required;

import starlight.domain.notification.entity.NotificationOutbox;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationOutboxQueryPort {

    NotificationOutbox findByIdOrThrow(Long outboxId);

    List<NotificationOutbox> findPublishableOutboxes(
            LocalDateTime now,
            LocalDateTime processingTimeoutThreshold,
            int maxRetryCount,
            int limit
    );
}
