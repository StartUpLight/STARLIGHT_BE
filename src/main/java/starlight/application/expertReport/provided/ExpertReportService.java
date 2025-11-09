package starlight.application.expertReport.provided;

import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportDetail;
import starlight.domain.expertReport.enumerate.SaveType;

import java.util.List;

public interface ExpertReportService{

    String createExpertReportLink(Long expertId, Long businessPlanId);

    ExpertReport getExpertReport(String token);

    void saveReport(String token, String overallComment, List<ExpertReportDetail> detail, SaveType saveType);
}