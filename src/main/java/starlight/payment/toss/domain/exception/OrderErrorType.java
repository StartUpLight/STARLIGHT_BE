package starlight.payment.toss.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum OrderErrorType implements ErrorType {

    TOSS_CLIENT_CONFIRM_ERROR(HttpStatus.BAD_REQUEST, "토스 결제 요청 중 오류가 발생했습니다."),
    TOSS_CLIENT_CANCEL_ERROR(HttpStatus.BAD_REQUEST, "토스 결제 취소 요청 중 오류가 발생했습니다.")
    ;

    private final HttpStatus status;

    private final String message;
}
