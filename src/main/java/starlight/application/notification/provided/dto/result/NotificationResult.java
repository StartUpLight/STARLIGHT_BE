package starlight.application.notification.provided.dto.result;

import starlight.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResult(
        Long id,
        String type,
        String title,
        String message,
        Long referenceId,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationResult from(Notification notification) {
        return new NotificationResult(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
