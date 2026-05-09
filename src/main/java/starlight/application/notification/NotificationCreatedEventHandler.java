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
import starlight.application.notification.required.NotificationPublishPort;
import starlight.application.notification.required.NotificationQueryPort;
import starlight.application.notification.required.dto.NotificationPublishMessage;
import starlight.domain.notification.entity.Notification;
import starlight.domain.notification.exception.NotificationException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCreatedEventHandler {

    private final NotificationQueryPort notificationQueryPort;
    private final NotificationPublishPort notificationPublishPort;

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
        Notification notification = notificationQueryPort.findByIdOrThrow(event.notificationId());
        NotificationPublishMessage message = NotificationPublishMessage.from(notification);

        notificationPublishPort.publish(message);

        log.info("[NOTIFICATION] published notificationId={}, memberId={}",
                message.notificationId(), message.memberId());
    }

    @Recover
    public void recover(Exception exception, NotificationCreatedEvent event) {
        log.error("[NOTIFICATION] publish failed after retries notificationId={}",
                event.notificationId(), exception);
    }
}
