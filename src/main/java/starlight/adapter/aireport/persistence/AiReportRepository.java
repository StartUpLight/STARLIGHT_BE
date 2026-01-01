package starlight.adapter.aireport.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.aireport.entity.AiReport;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface AiReportRepository extends JpaRepository<AiReport, Long> {

    Optional<AiReport> findByBusinessPlanId(Long businessPlanId);

    List<AiReport> findAllByBusinessPlanIdIn(Collection<Long> businessPlanIds);
}
