package starlight.adapter.expertReport.webapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import starlight.domain.expertReport.enumerate.SaveType;

import java.util.List;

public record UpsertExpertReportRequest(
        @NotNull(message = "저장 유형은 필수입니다")
        SaveType saveType,

        String overallComment,

        List<@Valid CreateExpertReportDetailRequest> details
) { }