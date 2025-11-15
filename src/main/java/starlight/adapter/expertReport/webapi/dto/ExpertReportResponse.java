package starlight.adapter.expertReport.webapi.dto;

import starlight.adapter.expert.webapi.dto.ExpertDetailResponse;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.enumerate.SubmitStatus;

import java.util.List;

public record ExpertReportResponse(
        ExpertDetailResponse expertDetailResponse,

        SubmitStatus status,

        boolean canEdit,

        String overallComment,

        List<ExpertReportDetailResponse> details
) {
    public static ExpertReportResponse fromEntities(ExpertReport report, Expert expert) {
        return new ExpertReportResponse(
                ExpertDetailResponse.from(expert),
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
                null,
                report.getSubmitStatus(),
                report.canEdit(),
                report.getOverallComment(),
                report.getDetails().stream()
                        .map(ExpertReportDetailResponse::from)
                        .toList()
        );
    }
}