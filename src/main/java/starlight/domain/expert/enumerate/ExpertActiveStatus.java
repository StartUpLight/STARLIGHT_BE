package starlight.domain.expert.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpertActiveStatus {

    ACTIVE("활동중"),
    INACTIVE("비활동중");

    private final String description;
}
