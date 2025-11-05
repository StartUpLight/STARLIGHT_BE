package starlight.domain.businessplan.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class BusinessPlanException extends GlobalException {

    public BusinessPlanException(ErrorType errorType) {
        super(errorType);
    }
}
