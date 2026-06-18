package starlight.adapter.notification.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import starlight.application.notification.required.NotificationOutboxCommandPort;
import starlight.application.notification.required.NotificationOutboxQueryPort;
import starlight.domain.notification.entity.NotificationOutbox;
import starlight.domain.notification.enumerate.NotificationOutboxStatus;
import starlight.domain.notification.exception.NotificationErrorType;
import starlight.domain.notification.exception.NotificationOutboxException;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationOutboxJpa implements NotificationOutboxCommandPort, NotificationOutboxQueryPort {

    private final NotificationOutboxRepository notificationOutboxRepository;

    @Override
    public NotificationOutbox save(NotificationOutbox outbox) {
        return notificationOutboxRepository.save(outbox);
    }

    @Override
    public NotificationOutbox findByIdOrThrow(Long outboxId) {
        return notificationOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new NotificationOutboxException(NotificationErrorType.NOTIFICATION_OUTBOX_NOT_FOUND));
    }

    @Override
    public List<NotificationOutbox> findPublishableOutboxes(
            LocalDateTime now,
            LocalDateTime processingTimeoutThreshold,
            int maxRetryCount,
            int limit
    ) {
        return notificationOutboxRepository.findPublishableOutboxes(
                List.of(NotificationOutboxStatus.PENDING, NotificationOutboxStatus.FAILED),
                NotificationOutboxStatus.PROCESSING,
                now,
                processingTimeoutThreshold,
                maxRetryCount,
                PageRequest.of(0, limit)
        );
    }
}
