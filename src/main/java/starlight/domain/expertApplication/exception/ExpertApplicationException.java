package starlight.domain.expertApplication.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class ExpertApplicationException extends GlobalException {

    public ExpertApplicationException(ErrorType errorType) {
        super(errorType);
    }
}
