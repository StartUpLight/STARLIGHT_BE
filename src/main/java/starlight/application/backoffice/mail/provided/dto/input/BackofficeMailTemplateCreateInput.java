package starlight.application.backoffice.mail.provided.dto.input;

import starlight.domain.backoffice.mail.BackofficeMailContentType;

public record BackofficeMailTemplateCreateInput(
        String name,
        String title,
        BackofficeMailContentType contentType,
        String html,
        String text
) {
}
