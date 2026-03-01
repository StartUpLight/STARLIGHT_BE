package starlight.adapter.backoffice.expertapplication.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import starlight.adapter.backoffice.expertapplication.webapi.dto.response.BackofficeBusinessPlanExpertApplicationsResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "[Office] 전문가 신청", description = "백오피스 전문가 신청/평가 조회 API")
public interface BackofficeExpertApplicationApiDoc {

    @Operation(summary = "사업계획서 전문가 연결/평가 조회", security = @SecurityRequirement(name = "backofficeSession"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(examples = @ExampleObject(
                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "사업계획서를 찾을 수 없습니다."
                                      }
                                    }
                                    """
                    ))
            )
    })
    @GetMapping("/v1/backoffice/business-plans/{planId}/expert-applications")
    ApiResponse<BackofficeBusinessPlanExpertApplicationsResponse> findBusinessPlanExpertApplications(
            @Parameter(description = "사업계획서 ID") @PathVariable Long planId
    );
}
