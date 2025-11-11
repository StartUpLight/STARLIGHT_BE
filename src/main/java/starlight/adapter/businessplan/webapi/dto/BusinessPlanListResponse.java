package starlight.adapter.businessplan.webapi.dto;

import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public record BusinessPlanListResponse(

        Long businessPlanId,

        String title,

        LocalDateTime lastSavedAt,

        PlanStatus planStatus
) {

    public static BusinessPlanListResponse from(BusinessPlan businessPlan) {
        LocalDateTime lastSavedAt = businessPlan.getModifiedAt() != null
                ? businessPlan.getModifiedAt()
                : businessPlan.getCreatedAt();

        return new BusinessPlanListResponse(
                businessPlan.getId(),
                businessPlan.getTitle(),
                lastSavedAt,
                businessPlan.getPlanStatus()
        );
    }

    public static List<BusinessPlanListResponse> fromAll(Collection<BusinessPlan> businessPlans) {
        return businessPlans.stream()
                .map(BusinessPlanListResponse::from)
                .toList();
    }
}

