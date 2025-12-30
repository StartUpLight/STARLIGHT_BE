package starlight.adapter.expertReport.webapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import starlight.domain.expertReport.enumerate.CommentType;

public record CreateExpertReportCommentRequest(
        @NotNull(message = "평가 타입은 필수입니다")
        CommentType type,

        @NotBlank(message = "내용은 필수입니다")
        String content
) { }
