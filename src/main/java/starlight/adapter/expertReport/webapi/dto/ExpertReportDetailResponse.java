package starlight.adapter.expertReport.webapi.dto;

import starlight.domain.expertReport.entity.ExpertReportDetail;
import starlight.domain.expertReport.enumerate.CommentType;

public record ExpertReportDetailResponse(
        CommentType commentType,

        String title,

        String content
) {
    public static ExpertReportDetailResponse from(ExpertReportDetail detail) {
        return new ExpertReportDetailResponse(
                detail.getCommentType(),
                detail.getTitle(),
                detail.getContent()
        );
    }
}