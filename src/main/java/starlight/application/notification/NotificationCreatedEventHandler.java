package starlight.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import starlight.application.notification.event.NotificationCreatedEvent;
import starlight.domain.notification.exception.NotificationException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCreatedEventHandler {

    private final NotificationOutboxPublishService notificationOutboxPublishService;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2),
            retryFor = {
                    RedisConnectionFailureException.class,
                    TransientDataAccessException.class
            },
            noRetryFor = {NotificationException.class}
    )
    public void handle(NotificationCreatedEvent event) {
        notificationOutboxPublishService.publish(event.outboxId());
    }

    @Recover
    public void recover(Exception exception, NotificationCreatedEvent event) {
        log.error("[NOTIFICATION] publish failed after retries outboxId={}",
                event.outboxId(), exception);
    }
}
