package starlight.application.aireport.required;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanQueryLookUpPort {
    BusinessPlan findByIdOrThrow(Long id);
}