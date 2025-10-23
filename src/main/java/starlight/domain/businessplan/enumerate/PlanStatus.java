package starlight.domain.businessplan.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanStatus {

    STARTED("시작됨"),
    DRAFTED("작성 완료"),
    AI_REVIEWED("AI 리뷰 완료"),
    EXPERT_MATCHED("전문가 매칭 완료"),
    FINALIZED("최종 완료");

    private final String description;
}