package starlight.adapter.aireport.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.aireport.required.AiReportQuery;
import starlight.domain.aireport.entity.AiReport;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiReportJpa implements AiReportQuery {

    private final AiReportRepository aiReportRepository;

    @Override
    public AiReport save(AiReport aiReport) {
        return aiReportRepository.save(aiReport);
    }

    @Override
    public Optional<AiReport> findByBusinessPlanId(Long businessPlanId) {
        return aiReportRepository.findByBusinessPlanId(businessPlanId);
    }
}

