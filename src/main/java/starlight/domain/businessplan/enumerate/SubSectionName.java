package starlight.domain.businessplan.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubSectionName {

    // 개요 (OVERVIEW)
    OVERVIEW_BASIC("개요", SectionName.OVERVIEW, "overview_basic"),

    // 문제 인식 (PROBLEM_RECOGNITION)
    PROBLEM_BACKGROUND("창업 배경 및 개발동기", SectionName.PROBLEM_RECOGNITION, "problem_background"),
    PROBLEM_PURPOSE("창업아이템의 목적 및 필요성", SectionName.PROBLEM_RECOGNITION, "problem_purpose"),
    PROBLEM_MARKET("창업아이템의 목표시장 분석", SectionName.PROBLEM_RECOGNITION, "problem_market"),

    // 실현 가능성 (FEASIBILITY)
    FEASIBILITY_STRATEGY("사업화 전략", SectionName.FEASIBILITY, "feasibility_strategy"),
    FEASIBILITY_MARKET("시장분석 및 경쟁력 확보 방안", SectionName.FEASIBILITY, "feasibility_market"),

    // 성장 전략 (GROWTH_STRATEGY)
    GROWTH_MODEL("비즈니스 모델", SectionName.GROWTH_STRATEGY, "growth_model"),
    GROWTH_FUNDING("자금조달 계획", SectionName.GROWTH_STRATEGY, "growth_funding"),
    GROWTH_ENTRY("시장진입 및 성과창출 전략", SectionName.GROWTH_STRATEGY, "growth_entry"),

    // 팀 역량 (TEAM_COMPETENCE)
    TEAM_FOUNDER("창업자의 역량", SectionName.TEAM_COMPETENCE, "team_founder"),
    TEAM_MEMBERS("팀 역량", SectionName.TEAM_COMPETENCE, "team_members");

    private final String description;
    private final SectionName sectionName;
    private final String tag; // RAG tag 용도
}