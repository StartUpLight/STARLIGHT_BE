package starlight.domain.expertApplication.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum ExpertApplicationErrorType implements ErrorType {

    EXPERT_APPLICATION_QUERY_ERROR(HttpStatus.NOT_FOUND, "전문가 정보를 조회하는 중에 오류가 발생했습니다."),
    EXPERT_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 전문가를 찾을 수 없습니다."),
    APPLICATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 신청한 전문가입니다."),
    FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일을 읽는 중에 오류가 발생했습니다."),
    EMAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중에 오류가 발생했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기는 최대 20MB까지 업로드 가능합니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원되지 않는 파일 형식입니다.")
    ;


    private final HttpStatus status;

    private final String message;
}
