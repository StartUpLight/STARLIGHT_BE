package starlight.application.businessplan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessPlanServiceImpl implements BusinessPlanService {

    private final BusinessPlanQuery businessPlanQuery;

    @Override
    public BusinessPlan createBusinessPlan(Long memberId) {
        BusinessPlan plan = BusinessPlan.create(memberId);

        return businessPlanQuery.save(plan);
    }

    @Override
    public void deleteBusinessPlan(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        if (!plan.isOwnedBy(memberId)) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNAUTHORIZED_ACCESS);
        }

        businessPlanQuery.delete(plan);
    }

    @Override
    public BusinessPlan updateBusinessPlanTitle(Long planId, Long memberId, String title){
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        if (!plan.isOwnedBy(memberId)) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNAUTHORIZED_ACCESS);
        }

        plan.updateTitle(title);

        return businessPlanQuery.save(plan);
    }
}
