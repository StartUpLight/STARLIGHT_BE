package starlight.domain.member.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class AuthException extends GlobalException {

    public AuthException(ErrorType errorType) {
        super(errorType);
    }
}
