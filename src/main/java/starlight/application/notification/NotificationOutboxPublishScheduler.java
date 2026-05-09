package starlight.application.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import starlight.application.notification.required.NotificationOutboxQueryPort;
import starlight.domain.notification.entity.NotificationOutbox;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "notification.outbox",
        name = "worker-enabled",
        havingValue = "true"
)
public class NotificationOutboxPublishScheduler {

    private final NotificationOutboxQueryPort notificationOutboxQueryPort;
    private final NotificationOutboxPublishService notificationOutboxPublishService;

    @Value("${notification.outbox.batch-size:50}")
    private int batchSize;

    @Value("${notification.outbox.max-retry-count:5}")
    private int maxRetryCount;

    @Value("${notification.outbox.processing-timeout-seconds:60}")
    private long processingTimeoutSeconds;

    @Scheduled(fixedDelayString = "${notification.outbox.publish-fixed-delay-ms:5000}")
    public void publishPendingOutboxes() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationOutbox> outboxes = notificationOutboxQueryPort.findPublishableOutboxes(
                now,
                now.minusSeconds(processingTimeoutSeconds),
                maxRetryCount,
                batchSize
        );

        for (NotificationOutbox outbox : outboxes) {
            try {
                notificationOutboxPublishService.publish(outbox.getId());
            } catch (Exception exception) {
                log.warn("[NOTIFICATION] outbox publish failed outboxId={}", outbox.getId(), exception);
            }
        }
    }
}
