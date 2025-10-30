package starlight.adapter.businessplan.webapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateRequest;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanResponse;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.shared.apiPayload.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/business-plans")
@Tag(name = "사업계획서", description = "사업계획서 API")
public class BusinessPlanController {

    private final BusinessPlanService businessPlanService;

    @Operation(summary = "사업 계획서를 삭제합니다.")
    @DeleteMapping("/{planId}")
    public ApiResponse<?> deleteBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        businessPlanService.deleteBusinessPlan(planId, authDetails.getMemberId());
        return ApiResponse.success();
    }

    @PostMapping
    @Operation(summary = "사업 계획서를 생성합니다.")
    public ApiResponse<?> createBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails
            ) {
        BusinessPlan businessPlan = businessPlanService.createBusinessPlan(authDetails.getMemberId());

        return ApiResponse.success(BusinessPlanResponse.from(businessPlan.getId(), businessPlan.getTitle(), businessPlan.getPlanStatus()));
    }

    @PatchMapping("/{planId}")
    @Operation(summary = "사업 계획서 제목을 수정합니다.")
    public ApiResponse<BusinessPlanResponse> updateBusinessPlanTitle(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestBody @Valid BusinessPlanCreateRequest request,
            @PathVariable Long planId
    ) {
        BusinessPlan businessPlan = businessPlanService.updateBusinessPlanTitle(planId, authDetails.getMemberId(), request.title());

        return ApiResponse.success(BusinessPlanResponse.from(businessPlan.getId(), businessPlan.getTitle(), businessPlan.getPlanStatus()));
    }
}
