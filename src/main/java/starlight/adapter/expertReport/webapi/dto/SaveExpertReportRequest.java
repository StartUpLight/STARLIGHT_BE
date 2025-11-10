package starlight.adapter.expertReport.webapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import starlight.domain.expertReport.enumerate.SaveType;

import java.util.List;

public record SaveExpertReportRequest(
        SaveType saveType,

        String overallComment,

        @NotEmpty(message = "최소 1개 이상의 평가 항목이 필요합니다")
        List<@Valid CreateExpertReportDetailRequest> details
) { }