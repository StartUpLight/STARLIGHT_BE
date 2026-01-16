package starlight.application.backoffice.mail.provided.dto.input;

public record BackofficeMailTemplateCreateInput(
        String name,
        String title,
        String contentType,
        String html,
        String text
) {
    public static BackofficeMailTemplateCreateInput of(
            String name,
            String title,
            String contentType,
            String html,
            String text
    ) {
        return new BackofficeMailTemplateCreateInput(name, title, contentType, html, text);
    }
}
