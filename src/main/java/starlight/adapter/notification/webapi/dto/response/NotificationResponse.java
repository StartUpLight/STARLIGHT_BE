package starlight.adapter.notification.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.notification.provided.dto.result.NotificationResult;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        Long referenceId,
        boolean read,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime readAt
) {
    public static NotificationResponse from(NotificationResult result) {
        return new NotificationResponse(
                result.id(),
                result.type(),
                result.title(),
                result.message(),
                result.referenceId(),
                result.read(),
                result.createdAt(),
                result.readAt()
        );
    }

    public static List<NotificationResponse> fromAll(List<NotificationResult> results) {
        return results.stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
