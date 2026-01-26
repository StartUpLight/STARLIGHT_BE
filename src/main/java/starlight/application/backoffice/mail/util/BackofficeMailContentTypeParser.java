package starlight.application.backoffice.mail.util;

import starlight.domain.backoffice.exception.BackofficeErrorType;
import starlight.domain.backoffice.exception.BackofficeException;
import starlight.domain.backoffice.mail.BackofficeMailContentType;

public final class BackofficeMailContentTypeParser {

    private BackofficeMailContentTypeParser() {}

    public static BackofficeMailContentType parse(String contentType) {
        try {
            return BackofficeMailContentType.from(contentType);
        } catch (IllegalArgumentException exception) {
            throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_CONTENT_TYPE);
        }
    }
}
