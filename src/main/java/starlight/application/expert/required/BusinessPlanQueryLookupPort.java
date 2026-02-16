package starlight.application.expert.required;

import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.List;

public interface BusinessPlanQueryLookupPort {

    List<BusinessPlan> findAllByMemberId(Long memberId);
}
