package starlight.domain.businessplan.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum BusinessPlanErrorType implements ErrorType {

    UNSUPPORTED_OPERATION(HttpStatus.BAD_REQUEST, "지원하지 않는 작업입니다."),
    DUPLICATE_SECTION_STRATEGY(HttpStatus.INTERNAL_SERVER_ERROR, "중복된 SectionStrategy가 존재합니다."),
    UNSUPPORTED_SECTION_STRATEGY(HttpStatus.BAD_REQUEST, "선택 가능한 섹션 전략이 존재하지 않습니다."),
    REQUEST_EMPTY_RAW_JSON(HttpStatus.BAD_REQUEST, "rawJson은 null 이 될 수 없습니다."),
    RAW_JSON_SERIALIZATION_FAILURE(HttpStatus.BAD_REQUEST, "rawJson 직렬화에 실패했습니다."),
    CHECKS_LIST_SIZE_INVALID(HttpStatus.BAD_REQUEST, "checks 리스트는 길이 5 여야 합니다."),
    SECTIONAL_CONTENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 Section 내용이 존재합니다."),
    SECTIONAL_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 Section 내용이 존재하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "권한이 없습니다.");
    ;

    private final HttpStatus status;

    private final String message;
}
