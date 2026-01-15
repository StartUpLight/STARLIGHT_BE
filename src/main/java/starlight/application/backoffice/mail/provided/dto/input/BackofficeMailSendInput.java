package starlight.application.backoffice.mail.provided.dto.input;

import java.util.List;

public record BackofficeMailSendInput(
        List<String> to,
        String subject,
        String contentType,
        String html,
        String text
) {
}
