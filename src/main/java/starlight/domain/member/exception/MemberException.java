package starlight.domain.member.exception;

import starlight.global.apiPayload.exception.ErrorType;
import starlight.global.apiPayload.exception.GlobalException;

public class MemberException extends GlobalException {

    public MemberException(ErrorType errorType) {
        super(errorType);
    }
}
