package starlight.adapter.businessplan.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "사업계획서", description = "사업계획서 API")
@SecurityRequirement(name = "bearerAuth")
public class BusinessPlanController {

    private final BusinessPlanUseCase businessPlanService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "사업 계획서 목록을 조회합니다. (마이페이지 용)")
    public ApiResponse<BusinessPlanResult.PreviewPage> getBusinessPlanList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Parameter(description = "페이지 번호 (1 이상 정수 / 기본 1)") @RequestParam(defaultValue = "1") @Min(1)int page,
            @Parameter(description = "페이지 크기 (1 이상 정수 / 기본 3)") @RequestParam(defaultValue = "3") @Min(1) int size
    ) {
        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, size);
        return ApiResponse.success(businessPlanService.getBusinessPlanList(
                authDetails.getMemberId(), pageable
        ));
    }

    @GetMapping("/{planId}/subsections")
    @Operation(summary = "사업 계획서의 제목과 모든 서브섹션 내용을 조회합니다. (미리보기 용)")
    public ApiResponse<BusinessPlanResult.Detail> getBusinessPlanDetail(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(businessPlanService.getBusinessPlanDetail(
                planId, authDetails.getMemberId()
        ));
    }

    @GetMapping("/{planId}/titles")
    @Operation(summary = "사업 계획서의 제목을 조회합니다.")
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
    @Operation(summary = "사업 계획서를 생성합니다.")
    public ApiResponse<BusinessPlanResult.Result> createBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        return ApiResponse.success(businessPlanService.createBusinessPlan(authDetails.getMemberId()));
    }

    @PostMapping("/pdf")
    @Operation(summary = "PDF URL을 기반으로 사업계획서를 생성합니다.")
    public ApiResponse<BusinessPlanResult.Result> createBusinessPlanWithPdfAndAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Valid @RequestBody BusinessPlanCreateWithPdfRequest request
    ) {
        return ApiResponse.success(businessPlanService.createBusinessPlanWithPdf(
                request.title(), request.pdfUrl(), authDetails.getMemberId()
        ));
    }

    @PatchMapping("/{planId}")
    @Operation(summary = "사업 계획서 제목을 수정합니다.")
    public ApiResponse<String> updateBusinessPlanTitle(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestBody @Valid BusinessPlanCreateRequest request,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(businessPlanService.updateBusinessPlanTitle(
                planId, request.title(), authDetails.getMemberId()
        ));
    }

    @Operation(summary = "사업 계획서를 삭제합니다.")
    @DeleteMapping("/{planId}")
    public ApiResponse<BusinessPlanResult.Result> deleteBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    ) {
        return ApiResponse.success(businessPlanService.deleteBusinessPlan(
                planId, authDetails.getMemberId()
        ));
    }

    @Operation(summary = "서브섹션을 생성 또는 수정합니다.")
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

    @Operation(summary = "서브섹션을 조회합니다.")
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

    @Operation(summary = "서브섹션의 체크리스트를 점검 후 업데이트합니다.")
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

    @Operation(summary = "서브섹션을 삭제합니다.")
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
