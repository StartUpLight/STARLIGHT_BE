package starlight.adapter.expertReport.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.expertReport.required.ExpertReportQuery;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.exception.ExpertReportErrorType;
import starlight.domain.expertReport.exception.ExpertReportException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertReportJpa implements ExpertReportQuery {

    private final ExpertReportRepository repository;

    @Override
    public ExpertReport getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ExpertReportException(ExpertReportErrorType.EXPERT_REPORT_NOT_FOUND)
        );
    }

    @Override
    public ExpertReport save(ExpertReport expertReport) {
        return repository.save(expertReport);
    }

    @Override
    public void delete(ExpertReport expertReport) {
        repository.delete(expertReport);
    }

    @Override
    public boolean existsByToken(String token) {
        return repository.existsByToken(token);
    }

    @Override
    public ExpertReport findByTokenWithDetails(String token) {
        return repository.findByToken(token).orElseThrow(
                () -> new ExpertReportException(ExpertReportErrorType.EXPERT_REPORT_NOT_FOUND)
        );
    }

    @Override
    public List<ExpertReport> findAllByBusinessPlanId(Long businessPlanId) {
        return repository.findAllByBusinessPlanIdOrderByCreatedAtDesc(businessPlanId);
    }
}
