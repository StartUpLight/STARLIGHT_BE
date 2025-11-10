package starlight.adapter.expertReport.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.expertReport.entity.ExpertReport;

import java.util.Optional;

public interface ExpertReportRepository extends JpaRepository<ExpertReport, Long> {

    boolean existsByToken(String token);

    @EntityGraph(attributePaths = {"details"})
    Optional<ExpertReport> findByToken(String token);
}
