package starlight.adapter.backoffice.mail.webapi.dto.response;

import starlight.application.backoffice.mail.provided.dto.result.BackofficeMailTemplateResult;

import java.time.LocalDateTime;

public record BackofficeMailTemplateResponse(
        Long id,
        String name,
        String title,
        String contentType,
        String html,
        String text,
        LocalDateTime createdAt
) {
    public static BackofficeMailTemplateResponse from(BackofficeMailTemplateResult result) {
        return new BackofficeMailTemplateResponse(
                result.id(),
                result.name(),
                result.title(),
                result.contentType(),
                result.html(),
                result.text(),
                result.createdAt()
        );
    }
}
