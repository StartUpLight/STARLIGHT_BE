package starlight.adapter.backoffice.mail.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailTemplateCreateInput;
import starlight.domain.backoffice.mail.BackofficeMailContentType;

public record BackofficeMailTemplateCreateRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "contentType is required")
        String contentType,
        String html,
        String text
) {
    public BackofficeMailTemplateCreateInput toInput() {
        return new BackofficeMailTemplateCreateInput(name, title, BackofficeMailContentType.from(contentType), html, text);
    }
}
