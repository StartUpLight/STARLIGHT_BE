package starlight.domain.expertReport.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SaveType {

    TEMPORARY("임시 저장"),
    FINAL("최종 제출");

    private final String description;
}
