package starlight.adapter.businessplan.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.domain.businessplan.entity.BusinessPlan;

@Repository
@RequiredArgsConstructor
public class BusinessPlanJpa implements BusinessPlanQuery {

    private final BusinessPlanRepository jpa;

    @Override
    public BusinessPlan getOrThrow(Long id) {
        return jpa.findById(id).orElseThrow(
                () -> new EntityNotFoundException("BusinessPlan not found: " + id)
        );
    }
}
