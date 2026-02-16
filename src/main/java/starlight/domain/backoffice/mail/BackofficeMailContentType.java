package starlight.domain.backoffice.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BackofficeMailContentType {
    HTML("html"),
    TEXT("텍스트");

    private final String description;

    public static BackofficeMailContentType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("contentType is required");
        }
        if ("html".equalsIgnoreCase(value)) {
            return HTML;
        }
        if ("text".equalsIgnoreCase(value)) {
            return TEXT;
        }
        throw new IllegalArgumentException("invalid contentType");
    }
}