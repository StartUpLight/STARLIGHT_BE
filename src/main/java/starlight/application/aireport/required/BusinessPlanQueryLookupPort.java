package starlight.application.aireport.required;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanQueryLookupPort {
    BusinessPlan findByIdOrThrow(Long id);
}
