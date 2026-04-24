package starlight.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.notification.enumerate.NotificationOutboxStatus;
import starlight.shared.AbstractEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        indexes = {
                @Index(name = "idx_notification_outbox_publishable", columnList = "status, next_retry_at, id"),
                @Index(
                        name = "idx_notification_outbox_processing_timeout",
                        columnList = "status, processing_started_at, id"
                ),
                @Index(name = "idx_notification_outbox_notification_id", columnList = "notification_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox extends AbstractEntity {

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at", nullable = false)
    private LocalDateTime nextRetryAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Version
    private Long version;

    public static NotificationOutbox create(Long notificationId, LocalDateTime now) {
        Assert.notNull(notificationId, "notificationId must not be null");
        Assert.notNull(now, "now must not be null");

        NotificationOutbox outbox = new NotificationOutbox();
        outbox.notificationId = notificationId;
        outbox.status = NotificationOutboxStatus.PENDING;
        outbox.retryCount = 0;
        outbox.nextRetryAt = now;

        return outbox;
    }

    public boolean canClaim(LocalDateTime now, LocalDateTime processingTimeoutThreshold, int maxRetryCount) {
        if (retryCount >= maxRetryCount) {
            return false;
        }

        if (status == NotificationOutboxStatus.PENDING || status == NotificationOutboxStatus.FAILED) {
            return !nextRetryAt.isAfter(now);
        }

        return status == NotificationOutboxStatus.PROCESSING
                && processingStartedAt != null
                && !processingStartedAt.isAfter(processingTimeoutThreshold);
    }

    public void markProcessing(LocalDateTime now) {
        Assert.notNull(now, "now must not be null");

        status = NotificationOutboxStatus.PROCESSING;
        processingStartedAt = now;
    }

    public void markSent(LocalDateTime now) {
        Assert.notNull(now, "now must not be null");

        status = NotificationOutboxStatus.SENT;
        sentAt = now;
        processingStartedAt = null;
        lastError = null;
    }

    public void markFailed(String errorMessage, LocalDateTime nextRetryAt) {
        Assert.notNull(nextRetryAt, "nextRetryAt must not be null");

        status = NotificationOutboxStatus.FAILED;
        retryCount++;
        this.nextRetryAt = nextRetryAt;
        processingStartedAt = null;
        lastError = truncate(errorMessage);
    }

    public void markFailedPermanently(String errorMessage) {
        status = NotificationOutboxStatus.DISCARDED;
        processingStartedAt = null;
        lastError = truncate(errorMessage);
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }

        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
