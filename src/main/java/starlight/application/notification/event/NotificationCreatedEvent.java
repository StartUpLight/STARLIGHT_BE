package starlight.application.notification.event;

public record NotificationCreatedEvent(
        Long outboxId
) {
    public static NotificationCreatedEvent of(Long outboxId) {
        return new NotificationCreatedEvent(outboxId);
    }
}
