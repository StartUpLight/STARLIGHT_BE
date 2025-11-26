package starlight.order.toss.domain.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class OrderException extends GlobalException {

    public OrderException(ErrorType errorType) {
        super(errorType);
    }
}
