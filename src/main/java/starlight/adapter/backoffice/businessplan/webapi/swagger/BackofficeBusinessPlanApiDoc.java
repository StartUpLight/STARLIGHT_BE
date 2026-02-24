package starlight.adapter.backoffice.businessplan.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.adapter.backoffice.businessplan.webapi.dto.response.BackofficeBusinessPlanDetailResponse;
import starlight.adapter.backoffice.businessplan.webapi.dto.response.BackofficeBusinessPlanDashboardResponse;
import starlight.adapter.backoffice.businessplan.webapi.dto.response.BackofficeBusinessPlanPageResponse;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.apiPayload.response.ApiResponse;

import java.time.LocalDate;

@Tag(name = "[Office] 사업계획서", description = "백오피스 사업계획서 관리 API")
public interface BackofficeBusinessPlanApiDoc {

    @Operation(summary = "사업계획서 상세 조회 (전문가 연결/평가 포함)", security = @SecurityRequirement(name = "backofficeSession"))
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
    @GetMapping("/v1/backoffice/business-plans/{planId}")
    ApiResponse<BackofficeBusinessPlanDetailResponse> findBusinessPlanDetail(
            @Parameter(description = "사업계획서 ID") @PathVariable Long planId
    );

    @Operation(summary = "사업계획서 목록 조회", security = @SecurityRequirement(name = "backofficeSession"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "INVALID_REQUEST_ARGUMENT",
                                        "message": "잘못된 요청 인자입니다."
                                      }
                                    }
                                    """
                    ))
            )
    })
    @GetMapping("/v1/backoffice/business-plans")
    ApiResponse<BackofficeBusinessPlanPageResponse> findBusinessPlans(
            @Parameter(description = "상태 필터") @RequestParam(required = false) PlanStatus status,
            @Parameter(description = "제목/작성자명/작성자이메일 통합검색") @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "updatedAt,desc") String sort
    );

    @Operation(summary = "사업계획서 대시보드 조회", security = @SecurityRequirement(name = "backofficeSession"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "INVALID_REQUEST_ARGUMENT",
                                        "message": "잘못된 요청 인자입니다."
                                      }
                                    }
                                    """
                    ))
            )
    })
    @GetMapping("/v1/backoffice/business-plans/dashboard")
    ApiResponse<BackofficeBusinessPlanDashboardResponse> getDashboard(
            @Parameter(description = "상태 필터") @RequestParam(required = false) PlanStatus status,
            @Parameter(description = "제목/작성자명/작성자이메일 통합검색") @RequestParam(required = false) String keyword,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    );
}
