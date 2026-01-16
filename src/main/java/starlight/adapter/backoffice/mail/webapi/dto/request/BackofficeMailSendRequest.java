package starlight.adapter.backoffice.mail.webapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;

import java.util.List;

public record BackofficeMailSendRequest(
        @NotEmpty(message = "to is required")
        List<@Email @NotBlank String> to,
        @NotBlank(message = "subject is required")
        String subject,
        @NotBlank(message = "contentType is required")
        String contentType,
        String html,
        String text
) {
    public BackofficeMailSendInput toInput() {
        return BackofficeMailSendInput.of(to, subject, contentType, html, text);
    }
}
