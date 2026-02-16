package starlight.adapter.businessplan.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateRequest;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateWithPdfRequest;
import starlight.adapter.businessplan.webapi.dto.SubSectionCreateRequest;
import starlight.adapter.businessplan.webapi.swagger.BusinessPlanApiDoc;
import starlight.application.businessplan.provided.dto.BusinessPlanResult;
import starlight.application.businessplan.provided.dto.SubSectionResult;
import starlight.application.businessplan.provided.BusinessPlanUseCase;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/business-plans")
public class BusinessPlanController implements BusinessPlanApiDoc {

    private final BusinessPlanUseCase businessPlanService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ApiResponse<BusinessPlanResult.PreviewPage> getBusinessPlanList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "3") @Min(1) int size
    ) {
        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, size);
        return ApiResponse.success(businessPlanService.getBusinessPlanList(
                authDetails.getMemberId(), pageable
        ));
    }

    @GetMapping("/{planId}/subsections")
    public ApiResponse<BusinessPlanResult.Detail> getBusinessPlanDetail(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(businessPlanService.getBusinessPlanDetail(
                planId, authDetails.getMemberId()
        ));
    }

    @GetMapping("/{planId}/titles")
    public ApiResponse<String> getBusinessPlanTitle(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(businessPlanService
                .getBusinessPlanInfo(planId, authDetails.getMemberId())
                .title()
        );
    }

    @PostMapping
    public ApiResponse<BusinessPlanResult.Result> createBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        return ApiResponse.success(businessPlanService.createBusinessPlan(authDetails.getMemberId()));
    }

    @PostMapping("/pdf")
    public ApiResponse<BusinessPlanResult.Result> createBusinessPlanWithPdfAndAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Valid @RequestBody BusinessPlanCreateWithPdfRequest request
    ) {
        return ApiResponse.success(businessPlanService.createBusinessPlanWithPdf(
                request.title(), request.pdfUrl(), authDetails.getMemberId()
        ));
    }

    @PatchMapping("/{planId}")
    public ApiResponse<String> updateBusinessPlanTitle(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestBody @Valid BusinessPlanCreateRequest request,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(businessPlanService.updateBusinessPlanTitle(
                planId, request.title(), authDetails.getMemberId()
        ));
    }

    @DeleteMapping("/{planId}")
    public ApiResponse<BusinessPlanResult.Result> deleteBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(businessPlanService.deleteBusinessPlan(
                planId, authDetails.getMemberId()
        ));
    }

    @PostMapping("/{planId}/subsections")
    public ApiResponse<SubSectionResult.Result> upsertSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionCreateRequest request
    ) {
        return ApiResponse.success(businessPlanService.upsertSubSection(
                planId, objectMapper.valueToTree(request), request.checks(), request.subSectionType(), authDetails.getMemberId()
        ));
    }

    @GetMapping("/{planId}/subsections/{subSectionType}")
    public ApiResponse<SubSectionResult.Detail> getSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @PathVariable SubSectionType subSectionType
    ) {
        return ApiResponse.success(businessPlanService.getSubSectionDetail(
                planId, subSectionType, authDetails.getMemberId()
        ));
    }

    @PostMapping("/{planId}/subsections/check-and-update")
    public ApiResponse<List<Boolean>> checkAndUpdateSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionCreateRequest request
    ) {
        return ApiResponse.success(businessPlanService.checkAndUpdateSubSection(
                planId, objectMapper.valueToTree(request), request.subSectionType(), authDetails.getMemberId()
        ));
    }

    @DeleteMapping("/{planId}/subsections/{subSectionType}")
    public ApiResponse<SubSectionResult.Result> deleteSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @PathVariable SubSectionType subSectionType
    ) {
        return ApiResponse.success(businessPlanService.deleteSubSection(
                planId, subSectionType, authDetails.getMemberId()
        ));
    }
}
