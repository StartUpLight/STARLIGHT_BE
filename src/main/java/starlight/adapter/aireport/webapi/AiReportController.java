package starlight.adapter.aireport.webapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateWithPdfRequest;
import starlight.application.aireport.provided.dto.AiReportResponse;
import starlight.application.aireport.provided.AiReportService;
import starlight.shared.apiPayload.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai-reports")
@Tag(name = "AI 리포트", description = "AI 리포트 채점 및 조회 API")
public class AiReportController {

    private final AiReportService aiReportService;

    @Operation(summary = "사업계획서를 AI로 채점 및 생성합니다.")
    @PostMapping("/evaluation/{planId}")
    public ApiResponse<AiReportResponse> gradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(aiReportService.gradeBusinessPlan(planId, authDetails.getMemberId()));
    }

    @Operation(summary = "PDF URL을 기반으로 사업계획서를 생성하고, AI로 채점 및 생성합니다.")
    @PostMapping("/evaluation/pdf")
    public ApiResponse<AiReportResponse> createAndGradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Valid @RequestBody BusinessPlanCreateWithPdfRequest request
    ) {
        return ApiResponse.success(aiReportService.createAndGradePdfBusinessPlan(
                request.title(),
                request.pdfUrl(),
                authDetails.getMemberId()
        ));
    }

    @Operation(summary = "AI 리포트를 조회합니다.")
    @GetMapping("/{planId}")
    public ApiResponse<AiReportResponse> getAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(aiReportService.getAiReport(planId, authDetails.getMemberId()));
    }
}

