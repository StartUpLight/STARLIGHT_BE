package starlight.application.businessplan.required;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanQuery {

    BusinessPlan getOrThrow(Long id);
}
