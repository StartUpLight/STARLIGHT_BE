package starlight.domain.notification.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class NotificationOutboxException extends GlobalException {

    public NotificationOutboxException(ErrorType errorType) {
        super(errorType);
    }
}
