package starlight.adapter.backoffice.mail.webapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.util.StringUtils;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;

import java.util.List;

public record BackofficeMailSendRequest(
        @NotEmpty(message = "to is required")
        List<@Email @NotBlank String> to,
        @NotBlank(message = "subject is required")
        String subject,
        @NotBlank(message = "contentType is required")
        @Pattern(regexp = "(?i)^(html|text)$", message = "contentType must be html or text")
        String contentType,
        String html,
        String text
) {
    @AssertTrue(message = "html is required for html contentType; text is required for text contentType")
    public boolean isBodyProvided() {
        if (!StringUtils.hasText(contentType)) {
            return true;
        }
        if ("html".equalsIgnoreCase(contentType)) {
            return StringUtils.hasText(html);
        }
        if ("text".equalsIgnoreCase(contentType)) {
            return StringUtils.hasText(text);
        }
        return true;
    }

    public BackofficeMailSendInput toInput() {
        return BackofficeMailSendInput.of(to, subject, contentType, html, text);
    }
}
