package starlight.application.expertReport.required;

import starlight.domain.expertReport.entity.ExpertReport;

import java.util.List;

public interface ExpertReportQueryPort {

    ExpertReport findByIdOrThrow(Long id);

    boolean existsByToken(String token);

    ExpertReport findByTokenWithCommentsOrThrow(String token);

    List<ExpertReport> findAllByBusinessPlanIdWithCommentsOrderByCreatedAtDesc(Long businessPlanId);
}
