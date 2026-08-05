package starlight.application.notification.required.dto;

import starlight.domain.notification.entity.Notification;

public record NotificationPublishMessage(
        Long memberId,
        Long notificationId,
        String type
) {
    public static NotificationPublishMessage from(Notification notification) {
        return new NotificationPublishMessage(
                notification.getMemberId(),
                notification.getId(),
                notification.getType()
        );
    }
}
