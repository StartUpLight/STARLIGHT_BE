package starlight.adapter.businessplan.webapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BusinessPlanCreateRequest (
    @NotBlank(message = "제목 입력은 필수입니다.")
    @Schema(description = "제목", example = "예비창업패키지 사업계획서")
    String title
) {}