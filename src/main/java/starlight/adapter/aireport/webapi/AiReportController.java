package starlight.adapter.aireport.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.aireport.webapi.dto.AiReportCreateWithPdfRequest;
import starlight.adapter.aireport.webapi.dto.AiReportResponse;
import starlight.adapter.aireport.webapi.swagger.AiReportApiDoc;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.application.aireport.provided.AiReportUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai-reports")
public class AiReportController implements AiReportApiDoc {

    private final AiReportUseCase aiReportUseCase;

    @PostMapping("/evaluation/{planId}")
    public ApiResponse<AiReportResponse> gradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(
                AiReportResponse.from(aiReportUseCase.gradeBusinessPlan(planId, authDetails.getMemberId()))
        );
    }

    @PostMapping("/evaluation/pdf")
    public ResponseEntity<ApiResponse<?>> createAndGradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Valid @RequestBody AiReportCreateWithPdfRequest request
    ) {
        aiReportUseCase.requestCreateAndGradePdfBusinessPlan(
                request.title(),
                request.pdfUrl(),
                authDetails.getMemberId()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success());
    }

    @GetMapping("/{planId}")
    public ApiResponse<AiReportResponse> getAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(
                AiReportResponse.from(aiReportUseCase.getAiReport(planId, authDetails.getMemberId()))
        );
    }
}
