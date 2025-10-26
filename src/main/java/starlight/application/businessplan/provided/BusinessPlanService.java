package starlight.application.businessplan.provided;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanService{

    BusinessPlan createBusinessPlan(Long memberId);

    void deleteBusinessPlan(Long planId, Long memberId);

    BusinessPlan updateBusinessPlanTitle(Long planId, Long memberId, String title);
}
