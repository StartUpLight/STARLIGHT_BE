package starlight.application.businessplan.required;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.Optional;

public interface BusinessPlanRepository extends JpaRepository<BusinessPlan, Long> {

    @EntityGraph(attributePaths = {"feasibility", "problemRecognition", "growthStrategy", "teamCompetence", "overview"})
    Optional<BusinessPlan> findById(Long id);

    default BusinessPlan getOrThrow(Long id) {
        return findById(id).orElseThrow(
                () -> new jakarta.persistence.EntityNotFoundException("BusinessPlan not found: " + id)
        );
    }
}
