package starlight.application.backoffice.expertapplication.required;

import starlight.domain.expertReport.entity.ExpertReport;

import java.util.List;

public interface ExpertReportLookupPort {

    List<ExpertReport> findAllByBusinessPlanIdWithCommentsOrderByCreatedAtDesc(Long businessPlanId);
}
