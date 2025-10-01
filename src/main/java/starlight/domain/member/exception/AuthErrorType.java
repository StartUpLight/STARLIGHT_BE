package starlight.domain.member.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.global.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum AuthErrorType implements ErrorType {

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_FORMAT_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 형식의 토큰입니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    UNAUTHORIZED_MEMBER_ACCESS(HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
