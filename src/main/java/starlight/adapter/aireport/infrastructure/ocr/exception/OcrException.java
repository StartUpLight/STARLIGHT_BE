package starlight.adapter.aireport.infrastructure.ocr.exception;

import starlight.shared.apiPayload.exception.ErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

public class OcrException extends GlobalException {
    public OcrException(ErrorType errorType) {
        super(errorType);
    }
}
