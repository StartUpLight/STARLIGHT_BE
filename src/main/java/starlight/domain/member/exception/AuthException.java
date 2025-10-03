package starlight.domain.member.exception;

import starlight.global.apiPayload.exception.ErrorType;
import starlight.global.apiPayload.exception.GlobalException;

public class AuthException extends GlobalException {

    public AuthException(ErrorType errorType) {
        super(errorType);
    }
}
