package starlight.global.apiPayload.exception;

import org.springframework.http.HttpStatus;

public interface ErrorType {

    String name();

    HttpStatus getStatus();

    String getMessage();
}
