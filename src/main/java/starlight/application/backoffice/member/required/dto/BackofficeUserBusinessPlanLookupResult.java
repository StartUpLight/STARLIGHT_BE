package starlight.application.backoffice.member.required.dto;

import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;

public record BackofficeUserBusinessPlanLookupResult(
        Long planId,
        String title,
        PlanStatus planStatus,
        LocalDateTime updatedAt
) {
    public static BackofficeUserBusinessPlanLookupResult of(
            Long planId,
            String title,
            PlanStatus planStatus,
            LocalDateTime updatedAt
    ) {
        return new BackofficeUserBusinessPlanLookupResult(planId, title, planStatus, updatedAt);
    }
}
