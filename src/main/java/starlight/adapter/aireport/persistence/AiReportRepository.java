package starlight.adapter.aireport.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.aireport.entity.AiReport;

import java.util.Optional;

public interface AiReportRepository extends JpaRepository<AiReport, Long> {
    Optional<AiReport> findByBusinessPlanId(Long businessPlanId);
}

