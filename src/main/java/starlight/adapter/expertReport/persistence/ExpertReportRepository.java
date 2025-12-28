package starlight.adapter.expertReport.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.expertReport.entity.ExpertReport;

import java.util.List;
import java.util.Optional;

public interface ExpertReportRepository extends JpaRepository<ExpertReport, Long> {

    boolean existsByToken(String token);

    @EntityGraph(attributePaths = {"comments"})
    Optional<ExpertReport> findByTokenWithComments(String token);

    @EntityGraph(attributePaths = {"comments"})
    List<ExpertReport> findAllByBusinessPlanIdOrderByCreatedAtDesc(Long businessPlanId);
}
