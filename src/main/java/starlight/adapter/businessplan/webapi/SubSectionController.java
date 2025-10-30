package starlight.adapter.businessplan.webapi;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.businessplan.webapi.dto.SubSectionRequest;
import starlight.adapter.businessplan.webapi.dto.SubSectionResponse;
import starlight.application.businessplan.provided.SubSectionService;
import starlight.domain.businessplan.enumerate.SubSectionName;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/business-plans/{planId}/subsections")
public class SubSectionController {

    private final SubSectionService subSectionService;

    @Operation(summary = "서브섹션을 생성 또는 수정합니다.")
    @PostMapping
    public ApiResponse<SubSectionResponse.Created> createOrUpdateSection(
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionRequest request) {
        return ApiResponse.success(subSectionService.createOrUpdateSection(planId, request));
    }

    @Operation(summary = "서브섹션을 조회합니다.")
    @GetMapping("/{subSectionName}")
    public ApiResponse<SubSectionResponse.Retrieved> getSubSection(
            @PathVariable Long planId,
            @PathVariable SubSectionName subSectionName) {
        return ApiResponse.success(subSectionService.getSubSection(planId, subSectionName));
    }

    @Operation(summary = "서브섹션을 삭제합니다.")
    @DeleteMapping("/{subSectionName}")
    public ApiResponse<SubSectionResponse.Deleted> deleteSubSection(
            @PathVariable Long planId,
            @PathVariable SubSectionName subSectionName) {
        return ApiResponse.success(subSectionService.deleteSubSection(planId, subSectionName));
    }

    @Operation(summary = "서브섹션의 체크리스트를 점검합니다.")
    @PostMapping("/check")
    public ApiResponse<List<Boolean>> checkSubSection(
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionRequest request) {
        return ApiResponse.success(subSectionService.checkSubSection(planId, request));
    }
}
