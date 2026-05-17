package starlight.application.notification.required.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationRealtimeMessage(
        Long memberId,
        Long notificationId,
        String type,
        String title,
        String message,
        Long referenceId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    public static NotificationRealtimeMessage from(Notification notification) {
        return new NotificationRealtimeMessage(
                notification.getMemberId(),
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.getCreatedAt()
        );
    }
}
