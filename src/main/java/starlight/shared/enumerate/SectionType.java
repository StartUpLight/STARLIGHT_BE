package starlight.shared.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SectionType {

    OVERVIEW("개요"),
    PROBLEM_RECOGNITION("문제 인식"),
    FEASIBILITY("실현 가능성"),
    GROWTH_STRATEGY("성장 전략"),
    TEAM_COMPETENCE("팀 역량");

    private final String description;
}