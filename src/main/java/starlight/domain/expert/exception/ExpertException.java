package starlight.domain.expert.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class ExpertException extends GlobalException {

    public ExpertException(ErrorType errorType) {
        super(errorType);
    }
}
