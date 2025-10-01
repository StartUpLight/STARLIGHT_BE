package starlight.domain.member.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.global.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum MemberErrorType implements ErrorType {

    INVALID_VERIFICATION_CODE(HttpStatus.UNAUTHORIZED, "유효하지 않은 이메일 인증 코드입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
