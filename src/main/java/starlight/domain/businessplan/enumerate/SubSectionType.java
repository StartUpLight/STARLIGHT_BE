package starlight.domain.businessplan.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import starlight.shared.enumerate.SectionType;

@Getter
@RequiredArgsConstructor
public enum SubSectionType {

    // 개요 (OVERVIEW)
    OVERVIEW_BASIC("개요", SectionType.OVERVIEW, "overview"),

    // 문제 인식 (PROBLEM_RECOGNITION)
    PROBLEM_BACKGROUND("창업 배경 및 개발동기", SectionType.PROBLEM_RECOGNITION, "problem_recognition"),
    PROBLEM_PURPOSE("창업아이템의 목적 및 필요성", SectionType.PROBLEM_RECOGNITION, "problem_recognition"),
    PROBLEM_MARKET("창업아이템의 목표시장 분석", SectionType.PROBLEM_RECOGNITION, "problem_recognition"),

    // 실현 가능성 (FEASIBILITY)
    FEASIBILITY_STRATEGY("사업화 전략", SectionType.FEASIBILITY, "feasibility"),
    FEASIBILITY_MARKET("시장분석 및 경쟁력 확보 방안", SectionType.FEASIBILITY, "feasibility"),

    // 성장 전략 (GROWTH_STRATEGY)
    GROWTH_MODEL("비즈니스 모델", SectionType.GROWTH_STRATEGY, "growth_tactic"),
    GROWTH_FUNDING("자금조달 계획", SectionType.GROWTH_STRATEGY, "growth_tactic"),
    GROWTH_ENTRY("시장진입 및 성과창출 전략", SectionType.GROWTH_STRATEGY, "growth_tactic"),

    // 팀 역량 (TEAM_COMPETENCE)
    TEAM_FOUNDER("창업자의 역량", SectionType.TEAM_COMPETENCE, "team_competence"),
    TEAM_MEMBERS("팀 역량", SectionType.TEAM_COMPETENCE, "team_competence");

    private final String description;
    private final SectionType sectionType;
    private final String tag; // RAG tag 용도
}