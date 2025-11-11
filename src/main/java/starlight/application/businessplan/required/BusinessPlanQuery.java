package starlight.application.businessplan.required;

import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.List;

public interface BusinessPlanQuery {

    BusinessPlan getOrThrow(Long id);

    BusinessPlan save(BusinessPlan businessPlan);

    void delete(BusinessPlan businessPlan);

    List<BusinessPlan> findAllByMemberIdOrderByModifiedAtDesc(Long memberId);
}
