package starlight.application.expertReport.provided;

import starlight.application.expertReport.provided.dto.ExpertReportWithExpertDto;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportComment;
import starlight.domain.expertReport.enumerate.SaveType;

import java.util.List;

public interface ExpertReportServiceUseCase{

    String createExpertReportLink(Long expertId, Long businessPlanId);

    ExpertReport saveReport(String token, String overallComment, List<ExpertReportComment> comments, SaveType saveType);

    ExpertReportWithExpertDto getExpertReportWithExpert(String token);

    List<ExpertReportWithExpertDto> getExpertReportsWithExpertByBusinessPlanId(Long businessPlanId);
}
