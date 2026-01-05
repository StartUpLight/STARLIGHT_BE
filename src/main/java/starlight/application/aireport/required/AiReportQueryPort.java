package starlight.application.aireport.required;

import starlight.domain.aireport.entity.AiReport;

import java.util.Optional;

public interface AiReportQueryPort {
    Optional<AiReport> findByBusinessPlanId(Long businessPlanId);
}

