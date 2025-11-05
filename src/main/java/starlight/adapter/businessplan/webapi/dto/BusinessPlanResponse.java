package starlight.adapter.businessplan.webapi.dto;

import starlight.domain.businessplan.enumerate.PlanStatus;

public record BusinessPlanResponse (
        Long businessPlanId,
        String title,
        PlanStatus planStatus
){
    public static BusinessPlanResponse from(Long businessPlanId, String title, PlanStatus planStatus) {
        return new BusinessPlanResponse(businessPlanId, title, planStatus);
    }
}
