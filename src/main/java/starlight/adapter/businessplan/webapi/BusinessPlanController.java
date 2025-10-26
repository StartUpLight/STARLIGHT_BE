package starlight.adapter.businessplan.webapi;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateRequest;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanResponse;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.provided.SectionCrudService;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/business-plans")
public class BusinessPlanController {

    private final SectionCrudService sectionCrudService;
    private final BusinessPlanService businessPlanService;

    @Operation(summary = "사업 계획서 안 쪽의 섹션(개요, 성장 전략, 팀 역량 등)을 조회합니다.")
    @GetMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Retrieved> getSection(
            @PathVariable Long planId,
            @RequestParam SectionName sectionName
    ) {
        return ApiResponse.success(sectionCrudService.getSection(planId, sectionName));
    }

    @Operation(summary = "사업 계획서 안 쪽의 섹션(개요, 성장 전략, 팀 역량 등)을 생성 및 수정합니다.")
    @PostMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Created> createOrUpdateSection(
            @PathVariable Long planId,
            @Valid @RequestBody SectionRequest request
    ) {
        return ApiResponse.success(sectionCrudService.createOrUpdateSection(planId, request));
    }

    @Operation(summary = "사업 계획서 안 쪽의 섹션(개요, 성장 전략, 팀 역량 등)을 삭제합니다.")
    @DeleteMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Deleted> deleteSection(
            @PathVariable Long planId,
            @RequestParam SectionName sectionName
    ) {
        return ApiResponse.success(sectionCrudService.deleteSection(planId, sectionName));
    }

    @Operation(summary = "사업 계획서 안 쪽의 섹션(개요, 성장 전략, 팀 역량 등)에서 체크리스트를 점검합니다.")
    @PostMapping("/section/check")
    public ApiResponse<List<Boolean>> deleteSection(
            @Valid @RequestBody SectionRequest request
    ) {
        return ApiResponse.success(sectionCrudService.checkSection(request));
    }

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
