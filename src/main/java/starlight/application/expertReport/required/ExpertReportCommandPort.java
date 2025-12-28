package starlight.application.expertReport.required;

import starlight.domain.expertReport.entity.ExpertReport;

public interface ExpertReportCommandPort {

    ExpertReport save(ExpertReport expertReport);

    void delete(ExpertReport expertReport);
}
