package starlight.shared.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SectionType {

    OVERVIEW("개요", null),
    PROBLEM_RECOGNITION("문제 인식", "problem_recognition"),
    FEASIBILITY("실현 가능성", "feasibility"),
    GROWTH_STRATEGY("성장 전략", "growth_strategy"),
    TEAM_COMPETENCE("팀 역량", "team_competence");

    private final String description;
    private final String tag;
}