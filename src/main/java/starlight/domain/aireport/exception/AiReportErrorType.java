package starlight.domain.aireport.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum AiReportErrorType implements ErrorType {

    AI_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 AI 리포트가 존재하지 않습니다."),
    NOT_READY_FOR_AI_REPORT(HttpStatus.BAD_REQUEST, "사업계획서가 작성 완료되지 않아 AI 리포트를 생성할 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "권한이 없습니다.");
    ;

    private final HttpStatus status;

    private final String message;
}
