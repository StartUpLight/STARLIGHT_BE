package starlight.domain.aireport.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class AiReportException extends GlobalException {

    public AiReportException(ErrorType errorType) {
        super(errorType);
    }

    public AiReportException(ErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
