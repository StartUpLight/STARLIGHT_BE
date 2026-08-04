package starlight.domain.notification.enumerate;

public enum NotificationOutboxStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    DISCARDED
}
