package starlight.application.backoffice.expertapplication.required;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanLookupPort {

    BusinessPlan findByIdOrThrow(Long planId);
}
