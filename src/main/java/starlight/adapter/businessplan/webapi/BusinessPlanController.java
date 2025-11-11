package starlight.adapter.businessplan.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateRequest;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanListResponse;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanResponse;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanSubSectionResponse;
import starlight.adapter.businessplan.webapi.dto.SubSectionRequest;
import starlight.application.businessplan.dto.SubSectionResponse;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/business-plans")
@Tag(name = "사업계획서", description = "사업계획서 API")
public class BusinessPlanController {

    private final BusinessPlanService businessPlanService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "사업 계획서 목록을 조회합니다. (마이페이지 용)")
    public ApiResponse<List<BusinessPlanListResponse>> getBusinessPlanList(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        List<BusinessPlan> businessPlans = businessPlanService.getBusinessPlanList(authDetails.getMemberId());
        return ApiResponse.success(BusinessPlanListResponse.fromAll(businessPlans));
    }

    @GetMapping("/{planId}/subsections")
    @Operation(summary = "사업 계획서의 모든 서브섹션을 조회합니다. (미리보기 용)")
    public ApiResponse<List<BusinessPlanSubSectionResponse>> getBusinessPlanSubSections(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(BusinessPlanSubSectionResponse.fromAll(
                businessPlanService.getBusinessPlanSubSections(planId, authDetails.getMemberId())
        ));
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

    @Operation(summary = "서브섹션을 생성 또는 수정합니다.")
    @PostMapping("/{planId}/subsections")
    public ApiResponse<SubSectionResponse.Created> createOrUpdateSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionRequest request
    ) {
        return ApiResponse.success(businessPlanService.createOrUpdateSubSection(
                planId, objectMapper.valueToTree(request), request.checks(), request.subSectionType(), authDetails.getMemberId()
        ));
    }

    @Operation(summary = "서브섹션을 조회합니다.")
    @GetMapping("/{planId}/subsections/{subSectionType}")
    public ApiResponse<SubSectionResponse.Retrieved> getSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @PathVariable SubSectionType subSectionType
    ) {
        return ApiResponse.success(businessPlanService.getSubSection(
                planId, subSectionType, authDetails.getMemberId())
        );
    }

    @Operation(summary = "서브섹션을 삭제합니다.")
    @DeleteMapping("/{planId}/subsections/{subSectionType}")
    public ApiResponse<SubSectionResponse.Deleted> deleteSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @PathVariable SubSectionType subSectionType
    ) {
        return ApiResponse.success(businessPlanService.deleteSubSection(
                planId, subSectionType, authDetails.getMemberId())
        );
    }

    @Operation(summary = "서브섹션의 체크리스트를 점검 후 업데이트합니다.")
    @PostMapping("/{planId}/subsections/check-and-update")
    public ApiResponse<java.util.List<Boolean>> checkAndUpdateSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionRequest request
    ) {
        return ApiResponse.success(businessPlanService.checkAndUpdateSubSection(
                planId, objectMapper.valueToTree(request), request.subSectionType(), authDetails.getMemberId()
        ));
    }
}
