package starlight.adapter.expertReport.webapi.dto;

import starlight.domain.expertReport.entity.ExpertReportComment;
import starlight.domain.expertReport.enumerate.CommentType;

public record ExpertReportCommentResponse(
        CommentType type,

        String content
) {
    public static ExpertReportCommentResponse from(ExpertReportComment comment) {
        return new ExpertReportCommentResponse(
                comment.getType(),
                comment.getContent()
        );
    }
}
