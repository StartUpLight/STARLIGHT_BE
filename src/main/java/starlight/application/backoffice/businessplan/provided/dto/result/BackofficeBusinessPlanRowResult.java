package starlight.application.backoffice.businessplan.provided.dto.result;

import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;

public record BackofficeBusinessPlanRowResult(
        Long planId,
        String title,
        PlanStatus planStatus,
        Long memberId,
        String memberName,
        String memberEmail,
        String signupChannel,
        LocalDateTime updatedAt,
        Integer score
) {
    public static BackofficeBusinessPlanRowResult of(
            Long planId,
            String title,
            PlanStatus planStatus,
            Long memberId,
            String memberName,
            String memberEmail,
            String signupChannel,
            LocalDateTime updatedAt,
            Integer score
    ) {
        return new BackofficeBusinessPlanRowResult(
                planId,
                title,
                planStatus,
                memberId,
                memberName,
                memberEmail,
                signupChannel,
                updatedAt,
                score
        );
    }
}
