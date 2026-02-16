package starlight.adapter.backoffice.mail.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailTemplateCreateInput;
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
        return BackofficeMailTemplateCreateInput.of(name, title, contentType, html, text);
    }
}
