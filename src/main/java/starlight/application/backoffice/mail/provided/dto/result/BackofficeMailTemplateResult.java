package starlight.application.backoffice.mail.provided.dto.result;

import java.time.LocalDateTime;

public record BackofficeMailTemplateResult(
        Long id,
        String name,
        String title,
        String contentType,
        String html,
        String text,
        LocalDateTime createdAt
) {
}
