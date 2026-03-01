package starlight.adapter.backoffice.businessplan.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanRowResult;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;

public record BackofficeBusinessPlanRowResponse(
        Long planId,
        String title,
        PlanStatus planStatus,
        Long memberId,
        String memberName,
        String memberEmail,
        String signupChannel,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        Integer score
) {
    public static BackofficeBusinessPlanRowResponse from(BackofficeBusinessPlanRowResult result) {
        return new BackofficeBusinessPlanRowResponse(
                result.planId(),
                result.title(),
                result.planStatus(),
                result.memberId(),
                result.memberName(),
                result.memberEmail(),
                result.signupChannel(),
                result.updatedAt(),
                result.score()
        );
    }
}
