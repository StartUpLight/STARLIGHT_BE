package starlight.domain.expertReport.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmitStatus {

    PENDING("평가 전"),
    TEMPORARY_SAVED("임시 저장"),
    SUBMITTED("제출 완료"),
    EXPIRED("만료됨");

    private final String description;
}