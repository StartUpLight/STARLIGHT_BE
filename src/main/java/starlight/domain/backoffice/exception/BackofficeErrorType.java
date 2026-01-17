package starlight.domain.backoffice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum BackofficeErrorType implements ErrorType {

    INVALID_MAIL_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 contentType입니다."),
    INVALID_MAIL_REQUEST(HttpStatus.BAD_REQUEST, "메일 발송 요청이 유효하지 않습니다."),
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송에 실패했습니다."),
    MAIL_TEMPLATE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 템플릿 저장에 실패했습니다."),
    MAIL_TEMPLATE_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 템플릿 조회에 실패했습니다."),
    MAIL_TEMPLATE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 템플릿 삭제에 실패했습니다."),
    MAIL_LOG_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 로그 저장에 실패했습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
