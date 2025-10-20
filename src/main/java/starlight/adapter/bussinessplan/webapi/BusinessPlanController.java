package starlight.adapter.bussinessplan.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.application.bussinessplan.provided.BusinessPlanService;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.dto.SectionResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/business-plans")
public class BusinessPlanController {

    private final BusinessPlanService businessPlanService;

    @PostMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Created> createSection(
            @PathVariable Long planId,
            @Valid @RequestBody SectionRequest request
    ) {
        return ApiResponse.success(businessPlanService.createSection(planId, request));
    }

    @GetMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Retrieved> getSection(
            @PathVariable Long planId,
            @RequestParam SectionName sectionName
    ) {
        return ApiResponse.success(businessPlanService.getSection(planId, sectionName));
    }

    @PutMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Updated> updateSection(
            @PathVariable Long planId,
            @Valid @RequestBody SectionRequest request
    ) {
        return ApiResponse.success(businessPlanService.updateSection(planId, request));
    }

    @DeleteMapping("/{planId}/section")
    public ApiResponse<SectionResponse.Deleted> deleteSection(
            @PathVariable Long planId,
            @RequestParam SectionName sectionName
    ) {
        return ApiResponse.success(businessPlanService.deleteSection(planId, sectionName));
    }
}
