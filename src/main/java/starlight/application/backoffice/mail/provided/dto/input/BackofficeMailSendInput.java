package starlight.application.backoffice.mail.provided.dto.input;

import java.util.List;

public record BackofficeMailSendInput(
        List<String> to,
        String subject,
        String contentType,
        String html,
        String text
) {
    public static BackofficeMailSendInput of(
            List<String> to,
            String subject,
            String contentType,
            String html,
            String text
    ) {
        return new BackofficeMailSendInput(to, subject, contentType, html, text);
    }
}
