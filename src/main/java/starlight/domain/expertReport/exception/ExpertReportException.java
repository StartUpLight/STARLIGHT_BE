package starlight.domain.expertReport.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class ExpertReportException extends GlobalException {

    public ExpertReportException(ErrorType errorType) {
        super(errorType);
    }
}
