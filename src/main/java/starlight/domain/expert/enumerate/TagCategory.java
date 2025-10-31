package starlight.domain.expert.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagCategory {

    MARKET_BM("시장성/BM"),
    TEAM_CAPABILITY("팀 역량"),
    PROBLEM_DEFINITION("문제 정의"),
    GROWTH_STRATEGY("성장 전략"),
    METRIC_DATA("지표/데이터"),;

    private final String description;
}