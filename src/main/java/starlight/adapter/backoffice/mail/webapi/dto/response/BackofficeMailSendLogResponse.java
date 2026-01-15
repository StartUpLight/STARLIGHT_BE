package starlight.adapter.backoffice.mail.webapi.dto.response;

import starlight.application.backoffice.mail.provided.dto.result.BackofficeMailSendLogResult;

import java.time.LocalDateTime;

public record BackofficeMailSendLogResponse(
        String recipients,
        String subject,
        String contentType,
        boolean success,
        String errorMessage
) {
    public static BackofficeMailSendLogResponse from(BackofficeMailSendLogResult result) {
        return new BackofficeMailSendLogResponse(
                result.recipients(),
                result.subject(),
                result.contentType(),
                result.success(),
                result.errorMessage()
        );
    }
}
