package starlight.adapter.aireport.webapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.application.aireport.dto.AiReportResponse;
import starlight.application.aireport.provided.AiReportService;
import starlight.shared.apiPayload.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai-reports/{planId}")
@Tag(name = "AI 리포트", description = "AI 리포트 채점 및 조회 API")
public class AiReportController {

    private final AiReportService aiReportService;

    @Operation(summary = "사업계획서를 AI로 채점 및 생성합니다.")
    @PostMapping("/grade")
    public ApiResponse<AiReportResponse> gradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(aiReportService.gradeBusinessPlan(planId, authDetails.getMemberId()));
    }

    @Operation(summary = "AI 리포트를 조회합니다.")
    @GetMapping
    public ApiResponse<AiReportResponse> getAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(aiReportService.getAiReport(planId, authDetails.getMemberId()));
    }
}

