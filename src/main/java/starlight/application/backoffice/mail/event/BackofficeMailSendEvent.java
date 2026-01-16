package starlight.application.backoffice.mail.event;

import starlight.domain.backoffice.mail.BackofficeMailContentType;

import java.util.List;

public record BackofficeMailSendEvent(
        List<String> to,
        String subject,
        BackofficeMailContentType contentType,
        boolean success,
        String errorMessage
) {
    public static BackofficeMailSendEvent of(
            List<String> to,
            String subject,
            BackofficeMailContentType contentType,
            boolean success,
            String errorMessage
    ) {
        return new BackofficeMailSendEvent(to, subject, contentType, success, errorMessage);
    }
}
