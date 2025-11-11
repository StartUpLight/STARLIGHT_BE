package starlight.adapter.expertReport.webapi.dto;

import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.enumerate.SubmitStatus;

import java.util.List;

public record ExpertReportResponse(
        String expertName,

        SubmitStatus status,

        boolean canEdit,

        String overallComment,

        List<ExpertReportDetailResponse> details
) {
    public static ExpertReportResponse of(ExpertReport report, String expertName) {
        return new ExpertReportResponse(
                expertName,
                report.getSubmitStatus(),
                report.canEdit(),
                report.getOverallComment(),
                report.getDetails().stream()
                        .map(ExpertReportDetailResponse::from)
                        .toList()
        );
    }

    public static ExpertReportResponse from(ExpertReport report) {
        return new ExpertReportResponse(
                report.getToken(),
                report.getSubmitStatus(),
                report.canEdit(),
                report.getOverallComment(),
                report.getDetails().stream()
                        .map(ExpertReportDetailResponse::from)
                        .toList()
        );
    }
}