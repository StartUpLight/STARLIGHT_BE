package starlight.shared.apiPayload.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final ErrorType errorType;

    public GlobalException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    public GlobalException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
}