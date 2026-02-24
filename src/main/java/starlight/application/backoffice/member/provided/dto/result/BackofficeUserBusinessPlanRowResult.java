package starlight.application.backoffice.member.provided.dto.result;

import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;

public record BackofficeUserBusinessPlanRowResult(
        Long planId,
        String title,
        PlanStatus planStatus,
        Integer score,
        LocalDateTime updatedAt
) {
    public static BackofficeUserBusinessPlanRowResult of(
            Long planId,
            String title,
            PlanStatus planStatus,
            Integer score,
            LocalDateTime updatedAt
    ) {
        return new BackofficeUserBusinessPlanRowResult(planId, title, planStatus, score, updatedAt);
    }
}
