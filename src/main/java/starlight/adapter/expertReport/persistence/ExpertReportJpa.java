package starlight.adapter.expertReport.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.expert.required.ExpertQuery;
import starlight.application.expertReport.required.ExpertReportQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.exception.ExpertReportErrorType;
import starlight.domain.expertReport.exception.ExpertReportException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertReportJpa implements ExpertReportQuery {

    private final ExpertReportRepository repository;

    @Override
    public ExpertReport getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND)
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
}
