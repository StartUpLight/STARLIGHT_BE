package starlight.application.expertReport.provided;

import starlight.application.expertReport.provided.dto.ExpertReportWithExpertResult;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportComment;
import starlight.domain.expertReport.enumerate.SaveType;

import java.util.List;

public interface ExpertReportUseCase {

    String createExpertReportLink(Long expertId, Long businessPlanId);

    ExpertReport saveReport(String token, String overallComment, List<ExpertReportComment> comments, SaveType saveType);

    ExpertReportWithExpertResult getExpertReportWithExpert(String token);

    List<ExpertReportWithExpertResult> getExpertReportsWithExpertByBusinessPlanId(Long businessPlanId);
}
