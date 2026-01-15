package starlight.application.backoffice.mail.provided.dto.result;

import java.time.LocalDateTime;

public record BackofficeMailSendLogResult(
        Long id,
        String recipients,
        String subject,
        String contentType,
        boolean success,
        String errorMessage,
        LocalDateTime createdAt
) {
}
