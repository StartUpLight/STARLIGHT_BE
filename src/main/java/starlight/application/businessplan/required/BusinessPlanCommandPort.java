package starlight.application.businessplan.required;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanCommandPort {

    BusinessPlan save(BusinessPlan businessPlan);

    void delete(BusinessPlan businessPlan);
}
