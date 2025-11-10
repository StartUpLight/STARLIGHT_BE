package starlight.adapter.expertReport.webapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import starlight.domain.expertReport.enumerate.CommentType;

public record CreateExpertReportDetailRequest(
        @NotNull(message = "평가 타입은 필수입니다")
        CommentType commentType,

        @NotBlank(message = "제목은 필수입니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content
) { }