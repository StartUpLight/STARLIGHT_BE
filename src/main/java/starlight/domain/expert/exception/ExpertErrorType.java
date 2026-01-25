package starlight.domain.expert.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum ExpertErrorType implements ErrorType {

    EXPERT_QUERY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "전문가 정보를 조회하는 중에 오류가 발생했습니다."),
    EXPERT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 전문가를 찾을 수 없습니다."),
    EXPERT_CAREER_INVALID(HttpStatus.BAD_REQUEST, "경력 정보가 올바르지 않습니다.");
    ;

    private final HttpStatus status;

    private final String message;
}
