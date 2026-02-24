package starlight.adapter.backoffice.member.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserBusinessPlanPageResponse;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserDashboardResponse;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserPageResponse;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserPaymentResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "[Office] 사용자", description = "백오피스 사용자 조회 API")
public interface BackofficeUserApiDoc {

    @Operation(summary = "사용자 대시보드 조회", security = @SecurityRequirement(name = "backofficeSession"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/dashboard")
    ApiResponse<BackofficeUserDashboardResponse> getDashboard();

    @Operation(summary = "사용자 목록 조회", security = @SecurityRequirement(name = "backofficeSession"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping
    ApiResponse<BackofficeUserPageResponse> findUsers(
            @Parameter(description = "이름/이메일 검색") @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "joinedAt,desc") String sort
    );

    @Operation(summary = "사용자별 사업계획서 조회", security = @SecurityRequirement(name = "backofficeSession"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{userId}/business-plans")
    ApiResponse<BackofficeUserBusinessPlanPageResponse> findUserBusinessPlans(
            @PathVariable Long userId,
            @Parameter(description = "점수 필터(all|scored|unscored)")
            @RequestParam(required = false, defaultValue = "all") String scoreFilter,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "updatedAt,desc") String sort
    );

    @Operation(summary = "사용자별 결제 조회", security = @SecurityRequirement(name = "backofficeSession"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{userId}/payments")
    ApiResponse<BackofficeUserPaymentResponse> findUserPayments(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort
    );
}
