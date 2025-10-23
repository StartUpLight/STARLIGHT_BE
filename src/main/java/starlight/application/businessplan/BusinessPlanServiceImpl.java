package starlight.application.businessplan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.domain.businessplan.entity.BusinessPlan;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessPlanServiceImpl implements BusinessPlanService {

    private final BusinessPlanQuery businessPlanQuery;

    public Long createBusinessPlan(Long memberId, String title) {
        BusinessPlan plan = BusinessPlan.create(memberId, title);

        return businessPlanQuery.save(plan).getId();
    }

    public void deleteBusinessPlan(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        if (!plan.isOwnedBy(memberId)) {
            throw new IllegalArgumentException("You do not have permission to delete this business plan.");
        }

        businessPlanQuery.delete(plan);
    }
}
