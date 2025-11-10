package starlight.adapter.expertReport.webapi.dto;

import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.enumerate.SubmitStatus;

import java.util.List;

public record ExpertReportResponse(
        SubmitStatus status,

        boolean canEdit,

        String overallComment,

        List<ExpertReportDetailResponse> details
) {
    public static ExpertReportResponse from(ExpertReport report) {
        return new ExpertReportResponse(
                report.getSubmitStatus(),
                report.canEdit(),
                report.getOverallComment(),
                report.getDetails().stream()
                        .map(ExpertReportDetailResponse::from)
                        .toList()
        );
    }
}