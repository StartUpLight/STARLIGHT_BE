package starlight.application.businessplan.provided;

public interface BusinessPlanService{

    Long createBusinessPlan(Long memberId);

    void deleteBusinessPlan(Long planId, Long memberId);
}
