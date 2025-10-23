package starlight.application.businessplan.provided;

public interface BusinessPlanService{

    Long createBusinessPlan(Long memberId, String title);

    void deleteBusinessPlan(Long planId, Long memberId);
}
