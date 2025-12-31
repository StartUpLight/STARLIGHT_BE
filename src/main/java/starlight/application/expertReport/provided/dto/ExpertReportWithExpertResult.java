package starlight.application.expertReport.provided.dto;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expertReport.entity.ExpertReport;

public record ExpertReportWithExpertResult(
        ExpertReport report,

        Expert expert
) {
    public static ExpertReportWithExpertResult of(ExpertReport report, Expert expert) {
        return new ExpertReportWithExpertResult(report, expert);
    }
}
