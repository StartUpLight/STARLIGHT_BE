package starlight.adapter.shared.infrastructure.pdf.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class PdfDownloadException extends GlobalException {
    public PdfDownloadException(ErrorType errorType) {
        super(errorType);
    }

    public PdfDownloadException(ErrorType errorType, Throwable cause) {
        super(errorType, cause);
    }
}
