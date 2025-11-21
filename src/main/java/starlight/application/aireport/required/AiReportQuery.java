package starlight.application.aireport.required;

import starlight.domain.aireport.entity.AiReport;

import java.util.Optional;

public interface AiReportQuery {
    AiReport save(AiReport aiReport);
    Optional<AiReport> findByBusinessPlanId(Long businessPlanId);
}

