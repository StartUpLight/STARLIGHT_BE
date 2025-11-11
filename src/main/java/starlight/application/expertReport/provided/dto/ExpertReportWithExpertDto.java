package starlight.application.expertReport.provided.dto;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expertReport.entity.ExpertReport;

public record ExpertReportWithExpertDto(
        ExpertReport report,

        Expert expert
) {
    public static ExpertReportWithExpertDto of(ExpertReport report, Expert expert) {
        return new ExpertReportWithExpertDto(report, expert);
    }
}
