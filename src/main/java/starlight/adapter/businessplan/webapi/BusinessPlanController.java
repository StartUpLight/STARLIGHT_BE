package starlight.adapter.businessplan.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.provided.SectionCrudService;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/business-plans")
public class BusinessPlanController {

    private final SectionCrudService sectionCrudService;
    private final BusinessPlanService businessPlanService;

    @PostMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Created> createSection(
            @PathVariable Long planId,
            @Valid @RequestBody SectionRequest request
    ) {
        return ApiResponse.success(sectionCrudService.createSection(planId, request));
    }

    @GetMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Retrieved> getSection(
            @PathVariable Long planId,
            @RequestParam SectionName sectionName
    ) {
        return ApiResponse.success(sectionCrudService.getSection(planId, sectionName));
    }

    @PutMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Updated> updateSection(
            @PathVariable Long planId,
            @Valid @RequestBody SectionRequest request
    ) {
        return ApiResponse.success(sectionCrudService.updateSection(planId, request));
    }

    @DeleteMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Deleted> deleteSection(
            @PathVariable Long planId,
            @RequestParam SectionName sectionName
    ) {
        return ApiResponse.success(sectionCrudService.deleteSection(planId, sectionName));
    }

    @DeleteMapping("/{planId}")
    public ApiResponse<?> deleteSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        businessPlanService.deleteBusinessPlan(planId, authDetails.getMemberId());
        return ApiResponse.success();
    }

    @PostMapping
    public ApiResponse<?> createBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        return ApiResponse.success(businessPlanService.createBusinessPlan(authDetails.getMemberId()));
    }
}
