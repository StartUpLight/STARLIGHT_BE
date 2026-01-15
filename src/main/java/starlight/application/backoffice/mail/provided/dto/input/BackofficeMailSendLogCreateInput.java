package starlight.application.backoffice.mail.provided.dto.input;

import starlight.domain.backoffice.mail.BackofficeMailContentType;

import java.util.List;

public record BackofficeMailSendLogCreateInput(
        List<String> to,
        String subject,
        BackofficeMailContentType contentType,
        boolean success,
        String errorMessage
) {
}
