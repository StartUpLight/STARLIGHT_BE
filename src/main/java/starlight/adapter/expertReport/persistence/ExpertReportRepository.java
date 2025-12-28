package starlight.adapter.expertReport.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.expertReport.entity.ExpertReport;

import java.util.List;
import java.util.Optional;

public interface ExpertReportRepository extends JpaRepository<ExpertReport, Long> {

    boolean existsByToken(String token);

    @EntityGraph(attributePaths = {"details"})
    Optional<ExpertReport> findByTokenWithDetails(String token);

    @EntityGraph(attributePaths = {"details"})
    List<ExpertReport> findAllByBusinessPlanIdOrderByCreatedAtDesc(Long businessPlanId);
}
