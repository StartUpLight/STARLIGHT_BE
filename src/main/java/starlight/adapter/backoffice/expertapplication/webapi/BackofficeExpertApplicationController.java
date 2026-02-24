package starlight.adapter.backoffice.expertapplication.webapi;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.backoffice.expertapplication.webapi.dto.response.BackofficeBusinessPlanExpertApplicationsResponse;
import starlight.adapter.backoffice.expertapplication.webapi.swagger.BackofficeExpertApplicationApiDoc;
import starlight.application.backoffice.expertapplication.provided.BackofficeExpertApplicationQueryUseCase;
import starlight.application.backoffice.expertapplication.provided.dto.result.BackofficeBusinessPlanExpertApplicationsResult;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
public class BackofficeExpertApplicationController implements BackofficeExpertApplicationApiDoc {

    private final BackofficeExpertApplicationQueryUseCase backofficeExpertApplicationQueryUseCase;

    @Override
    @GetMapping("/v1/backoffice/business-plans/{planId}/expert-applications")
    public ApiResponse<BackofficeBusinessPlanExpertApplicationsResponse> findBusinessPlanExpertApplications(
            @PathVariable Long planId
    ) {
        BackofficeBusinessPlanExpertApplicationsResult result =
                backofficeExpertApplicationQueryUseCase.findByBusinessPlanId(planId);
        return ApiResponse.success(BackofficeBusinessPlanExpertApplicationsResponse.from(result));
    }
}
