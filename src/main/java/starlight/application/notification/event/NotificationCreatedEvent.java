package starlight.application.notification.event;

public record NotificationCreatedEvent(
        Long notificationId
) {
    public static NotificationCreatedEvent of(Long notificationId) {
        return new NotificationCreatedEvent(notificationId);
    }
}
