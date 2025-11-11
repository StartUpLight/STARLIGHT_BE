package starlight.domain.member.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class MemberException extends GlobalException {

    public MemberException(ErrorType errorType) {
        super(errorType);
    }
}
