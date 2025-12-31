package starlight.adapter.expertReport.webapi.dto;

import starlight.application.expert.provided.dto.ExpertDetailResult;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.enumerate.SubmitStatus;

import java.util.List;

public record ExpertReportResponse(
        ExpertReportExpertResponse expertDetailResponse,

        SubmitStatus status,

        boolean canEdit,

        String overallComment,

        List<ExpertReportCommentResponse> comments
) {
    public static ExpertReportResponse fromEntities(ExpertReport report, Expert expert) {
        return new ExpertReportResponse(
                ExpertReportExpertResponse.from(ExpertDetailResult.from(expert, 0L)),
                report.getSubmitStatus(),
                report.canEdit(),
                report.getOverallComment(),
                report.getComments().stream()
                        .map(ExpertReportCommentResponse::from)
                        .toList()
        );
    }

    public static ExpertReportResponse from(ExpertReport report) {
        return new ExpertReportResponse(
                null,
                report.getSubmitStatus(),
                report.canEdit(),
                report.getOverallComment(),
                report.getComments().stream()
                        .map(ExpertReportCommentResponse::from)
                        .toList()
        );
    }
}
