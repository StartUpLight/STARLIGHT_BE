package starlight.adapter.backoffice.member.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserBusinessPlanRowResult;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;

public record BackofficeUserBusinessPlanRowResponse(
        Long planId,
        String title,
        PlanStatus planStatus,
        Integer score,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static BackofficeUserBusinessPlanRowResponse from(BackofficeUserBusinessPlanRowResult result) {
        return new BackofficeUserBusinessPlanRowResponse(
                result.planId(),
                result.title(),
                result.planStatus(),
                result.score(),
                result.updatedAt()
        );
    }
}
