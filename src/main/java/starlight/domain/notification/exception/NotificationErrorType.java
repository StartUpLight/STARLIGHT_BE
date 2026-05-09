package starlight.domain.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorType implements ErrorType {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 알림에 접근할 수 없습니다."),
    NOTIFICATION_OUTBOX_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 알림 발행 작업을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
