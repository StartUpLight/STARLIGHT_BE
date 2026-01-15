package starlight.domain.backoffice.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class BackofficeException extends GlobalException {

    public BackofficeException(ErrorType errorType) {
        super(errorType);
    }
}
