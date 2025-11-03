package starlight.adapter.businessplan.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.exception.BusinessPlanException;

import static starlight.domain.businessplan.exception.BusinessPlanErrorType.BUSINESS_PLAN_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class BusinessPlanJpa implements BusinessPlanQuery {

    private final BusinessPlanRepository businessPlanRepository;

    @Override
    public BusinessPlan getOrThrow(Long id) {
        return businessPlanRepository.findById(id).orElseThrow(
                () -> new BusinessPlanException(BUSINESS_PLAN_NOT_FOUND)
        );
    }

    @Override
    public BusinessPlan save(BusinessPlan businessPlan) {
        return businessPlanRepository.save(businessPlan);
    }

    @Override
    public void delete(BusinessPlan businessPlan) {
        businessPlanRepository.delete(businessPlan);
    }
}
