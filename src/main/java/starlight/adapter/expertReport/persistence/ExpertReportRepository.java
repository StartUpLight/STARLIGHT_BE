package starlight.adapter.expertReport.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expertReport.entity.ExpertReport;

import java.util.Optional;

public interface ExpertReportRepository extends JpaRepository<ExpertReport, Long> {

    boolean existsByToken(String token);

    @EntityGraph(attributePaths = {"details"})
    Optional<ExpertReport> findByToken(String token);
}
