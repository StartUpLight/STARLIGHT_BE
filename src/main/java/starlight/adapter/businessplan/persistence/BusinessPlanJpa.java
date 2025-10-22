package starlight.adapter.businessplan.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.domain.businessplan.entity.BusinessPlan;

@Repository
@RequiredArgsConstructor
public class BusinessPlanJpa implements BusinessPlanQuery {

    private final BusinessPlanRepository businessPlanRepository;

    @Override
    public BusinessPlan getOrThrow(Long id) {
        return businessPlanRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("BusinessPlan not found: " + id)
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
