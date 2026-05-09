package starlight.adapter.notification.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import starlight.application.notification.provided.dto.input.NotificationSendInput;

public record NotificationTestRequest(
        @NotBlank(message = "type is required")
        String type,

        @NotBlank(message = "title is required")
        String title,

        @NotBlank(message = "message is required")
        String message,

        Long referenceId
) {
    public NotificationSendInput toInput(Long memberId) {
        return NotificationSendInput.of(
                memberId,
                type,
                title,
                message,
                referenceId
        );
    }
}
