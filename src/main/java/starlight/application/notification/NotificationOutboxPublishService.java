package starlight.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import starlight.application.notification.required.NotificationOutboxCommandPort;
import starlight.application.notification.required.NotificationOutboxQueryPort;
import starlight.application.notification.required.NotificationPublishPort;
import starlight.application.notification.required.NotificationQueryPort;
import starlight.application.notification.required.dto.NotificationPublishMessage;
import starlight.domain.notification.entity.Notification;
import starlight.domain.notification.entity.NotificationOutbox;
import starlight.domain.notification.exception.NotificationException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxPublishService {

    private final NotificationOutboxQueryPort notificationOutboxQueryPort;
    private final NotificationOutboxCommandPort notificationOutboxCommandPort;
    private final NotificationQueryPort notificationQueryPort;
    private final NotificationPublishPort notificationPublishPort;
    private final TransactionTemplate transactionTemplate;

    @Value("${notification.outbox.max-retry-count:5}")
    private int maxRetryCount;

    @Value("${notification.outbox.retry-delay-seconds:10}")
    private long retryDelaySeconds;

    @Value("${notification.outbox.processing-timeout-seconds:60}")
    private long processingTimeoutSeconds;

    public boolean publish(Long outboxId) {
        NotificationOutbox outbox = claimOutbox(outboxId);
        if (outbox == null) {
            return false;
        }

        try {
            Notification notification = notificationQueryPort.findByIdOrThrow(outbox.getNotificationId());
            notificationPublishPort.publish(NotificationPublishMessage.from(notification));
            markSent(outbox.getId());

            log.info("[NOTIFICATION] outbox published outboxId={}, notificationId={}",
                    outbox.getId(), outbox.getNotificationId());

            return true;
        } catch (NotificationException exception) {
            markFailedPermanently(outbox.getId(), exception);
            return false;
        } catch (Exception exception) {
            markFailed(outbox.getId(), exception);
            throw exception;
        }
    }

    private NotificationOutbox claimOutbox(Long outboxId) {
        try {
            return transactionTemplate.execute(status -> {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime processingTimeoutThreshold = now.minusSeconds(processingTimeoutSeconds);
                NotificationOutbox outbox = notificationOutboxQueryPort.findByIdOrThrow(outboxId);

                if (!outbox.canClaim(now, processingTimeoutThreshold, maxRetryCount)) {
                    return null;
                }

                outbox.markProcessing(now);
                return notificationOutboxCommandPort.save(outbox);
            });
        } catch (OptimisticLockingFailureException exception) {
            log.debug("[NOTIFICATION] outbox already claimed outboxId={}", outboxId, exception);
            return null;
        }
    }

    private void markSent(Long outboxId) {
        transactionTemplate.executeWithoutResult(status -> {
            NotificationOutbox outbox = notificationOutboxQueryPort.findByIdOrThrow(outboxId);
            outbox.markSent(LocalDateTime.now());
            notificationOutboxCommandPort.save(outbox);
        });
    }

    private void markFailed(Long outboxId, Exception exception) {
        transactionTemplate.executeWithoutResult(status -> {
            NotificationOutbox outbox = notificationOutboxQueryPort.findByIdOrThrow(outboxId);
            outbox.markFailed(exception.getMessage(), LocalDateTime.now().plusSeconds(retryDelaySeconds));
            notificationOutboxCommandPort.save(outbox);
        });
    }

    private void markFailedPermanently(Long outboxId, Exception exception) {
        transactionTemplate.executeWithoutResult(status -> {
            NotificationOutbox outbox = notificationOutboxQueryPort.findByIdOrThrow(outboxId);
            outbox.markFailedPermanently(exception.getMessage());
            notificationOutboxCommandPort.save(outbox);
        });
    }
}
