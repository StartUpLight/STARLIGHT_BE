package starlight.application.expertReport.required;

import starlight.domain.expertReport.entity.ExpertReport;

import java.util.Optional;

public interface ExpertReportQuery {

    ExpertReport getOrThrow(Long id);

    ExpertReport save(ExpertReport expertReport);

    void delete(ExpertReport expertReport);

    boolean existsByToken(String token);

    ExpertReport findByTokenWithDetails(String token);
}
