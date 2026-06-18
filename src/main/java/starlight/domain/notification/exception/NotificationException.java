package starlight.domain.notification.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class NotificationException extends GlobalException {

    public NotificationException(ErrorType errorType) {
        super(errorType);
    }
}
