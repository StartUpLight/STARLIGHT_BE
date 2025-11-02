package starlight.adapter.businessplan.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.Optional;

public interface BusinessPlanRepository extends JpaRepository<BusinessPlan, Long> {

    @EntityGraph(attributePaths = { "feasibility", "problemRecognition", "growthTactic", "teamCompetence", "overview" })
    Optional<BusinessPlan> findById(Long id);
}
