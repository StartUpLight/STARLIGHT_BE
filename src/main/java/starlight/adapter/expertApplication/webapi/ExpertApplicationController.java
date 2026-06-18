package starlight.adapter.expertApplication.webapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.expertApplication.webapi.swagger.ExpertApplicationApiDoc;
import starlight.application.expertApplication.provided.ExpertApplicationCommandUseCase;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/expert-applications")
public class ExpertApplicationController implements ExpertApplicationApiDoc {

    private final ExpertApplicationCommandUseCase applicationServiceUseCase;

    @PostMapping("/{expertId}/request")
    public ApiResponse<String> requestFeedback(
            @PathVariable Long expertId,
            @RequestParam Long businessPlanId,
            @RequestParam(required = false) String pdfUrl,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        applicationServiceUseCase.requestFeedback(
                expertId, businessPlanId, pdfUrl, authenticatedMember.getMemberName()
        );
        return ApiResponse.success("피드백 요청이 전달되었습니다.");
    }
}
