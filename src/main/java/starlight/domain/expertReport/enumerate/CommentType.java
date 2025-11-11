package starlight.domain.expertReport.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentType {

    STRENGTH("강점"),
    WEAKNESS("약점");

    private final String description;
}