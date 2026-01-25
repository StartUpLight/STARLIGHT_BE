package starlight.adapter.aireport.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.aireport.webapi.swagger.AiReportApiDoc;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateWithPdfRequest;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.provided.AiReportUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai-reports")
public class AiReportController implements AiReportApiDoc {

    private final AiReportUseCase aiReportUseCase;

    @PostMapping("/evaluation/{planId}")
    public ApiResponse<AiReportResult> gradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(aiReportUseCase.gradeBusinessPlan(planId, authDetails.getMemberId()));
    }

    @PostMapping("/evaluation/pdf")
    public ApiResponse<AiReportResult> createAndGradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Valid @RequestBody BusinessPlanCreateWithPdfRequest request
    ) {
        return ApiResponse.success(aiReportUseCase.createAndGradePdfBusinessPlan(
                request.title(),
                request.pdfUrl(),
                authDetails.getMemberId()
        ));
    }

    @GetMapping("/{planId}")
    public ApiResponse<AiReportResult> getAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(aiReportUseCase.getAiReport(planId, authDetails.getMemberId()));
    }
}
