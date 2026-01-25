package starlight.adapter.businessplan.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateRequest;
import starlight.adapter.businessplan.webapi.dto.BusinessPlanCreateWithPdfRequest;
import starlight.adapter.businessplan.webapi.dto.SubSectionCreateRequest;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.application.businessplan.provided.dto.BusinessPlanResult;
import starlight.application.businessplan.provided.dto.SubSectionResult;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Tag(name = "사업계획서", description = "사업계획서 API")
@SecurityRequirement(name = "bearerAuth")
public interface BusinessPlanApiDoc {

    @Operation(
            summary = "사업 계획서 목록을 조회합니다. (마이페이지 용)",
            description = "로그인한 사용자의 사업계획서 목록을 페이지네이션으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BusinessPlanResult.PreviewPage.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping
    ApiResponse<BusinessPlanResult.PreviewPage> getBusinessPlanList(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Parameter(description = "페이지 번호 (1 이상 정수 / 기본 1)") @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "페이지 크기 (1 이상 정수 / 기본 3)") @RequestParam(defaultValue = "3") @Min(1) int size
    );

    @Operation(
            summary = "사업 계획서의 제목과 모든 서브섹션 내용을 조회합니다. (미리보기 용)",
            description = "지정된 사업계획서의 제목과 모든 서브섹션 내용을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BusinessPlanResult.Detail.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/{planId}/subsections")
    ApiResponse<BusinessPlanResult.Detail> getBusinessPlanDetail(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    );

    @Operation(
            summary = "사업 계획서의 제목을 조회합니다.",
            description = "지정된 사업계획서의 제목만 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "SUCCESS",
                                      "data": "나의 사업계획서",
                                      "error": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/{planId}/titles")
    ApiResponse<String> getBusinessPlanTitle(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    );

    @Operation(
            summary = "사업 계획서를 생성합니다.",
            description = "새로운 사업계획서를 생성합니다. 기본 제목은 사용자 이름 + '의 사업계획서'로 설정됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BusinessPlanResult.Result.class)
                    )
            )
    })
    @PostMapping
    ApiResponse<BusinessPlanResult.Result> createBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails
    );

    @Operation(
            summary = "PDF URL을 기반으로 사업계획서를 생성합니다.",
            description = "PDF URL을 제공하여 사업계획서를 생성합니다. PDF는 OCR을 통해 텍스트로 변환됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BusinessPlanResult.Result.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "VALIDATION_ERROR",
                                        "message": "요청 데이터가 유효하지 않습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/pdf")
    ApiResponse<BusinessPlanResult.Result> createBusinessPlanWithPdfAndAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Valid @RequestBody BusinessPlanCreateWithPdfRequest request
    );

    @Operation(
            summary = "사업 계획서 제목을 수정합니다.",
            description = "지정된 사업계획서의 제목을 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "SUCCESS",
                                      "data": "수정된 제목",
                                      "error": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @PatchMapping("/{planId}")
    ApiResponse<String> updateBusinessPlanTitle(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestBody @Valid BusinessPlanCreateRequest request,
            @PathVariable Long planId
    );

    @Operation(
            summary = "사업 계획서를 삭제합니다.",
            description = "지정된 사업계획서를 삭제합니다. 서브섹션도 함께 삭제됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BusinessPlanResult.Result.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @DeleteMapping("/{planId}")
    ApiResponse<BusinessPlanResult.Result> deleteBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    );

    @Operation(
            summary = "서브섹션을 생성 또는 수정합니다.",
            description = "지정된 사업계획서의 서브섹션을 생성하거나 수정합니다. 서브섹션이 존재하지 않으면 생성하고, 존재하면 업데이트합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubSectionResult.Result.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "rawJson 누락",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "REQUEST_EMPTY_RAW_JSON",
                                        "message": "rawJson은 null 이 될 수 없습니다."
                                      }
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "rawJson 직렬화 실패",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "RAW_JSON_SERIALIZATION_FAILURE",
                                        "message": "rawJson 직렬화에 실패했습니다."
                                      }
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "checks 리스트 크기 오류",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "CHECKS_LIST_SIZE_INVALID",
                                        "message": "checks 리스트는 길이 5 여야 합니다."
                                      }
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/{planId}/subsections")
    ApiResponse<SubSectionResult.Result> upsertSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionCreateRequest request
    );

    @Operation(
            summary = "서브섹션을 조회합니다.",
            description = "지정된 사업계획서의 특정 서브섹션을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubSectionResult.Detail.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "사업계획서 없음",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "서브섹션 없음",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "SUBSECTION_NOT_FOUND",
                                        "message": "해당 서브 섹션이 존재하지 않습니다."
                                      }
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/{planId}/subsections/{subSectionType}")
    ApiResponse<SubSectionResult.Detail> getSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @PathVariable SubSectionType subSectionType
    );

    @Operation(
            summary = "서브섹션의 체크리스트를 점검 후 업데이트합니다.",
            description = "서브섹션의 내용을 AI로 체크리스트 점검한 후 결과를 업데이트합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "SUCCESS",
                                      "data": [true, false, true, false, true],
                                      "error": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "CHECKS_LIST_SIZE_INVALID",
                                        "message": "checks 리스트는 길이 5 여야 합니다."
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "사업계획서 없음",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "서브섹션 없음",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "SUBSECTION_NOT_FOUND",
                                        "message": "해당 서브 섹션이 존재하지 않습니다."
                                      }
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/{planId}/subsections/check-and-update")
    ApiResponse<List<Boolean>> checkAndUpdateSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @Valid @RequestBody SubSectionCreateRequest request
    );

    @Operation(
            summary = "서브섹션을 삭제합니다.",
            description = "지정된 사업계획서의 특정 서브섹션을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubSectionResult.Result.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "사업계획서 없음",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "BUSINESS_PLAN_NOT_FOUND",
                                        "message": "해당 사업계획서가 존재하지 않습니다."
                                      }
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "서브섹션 없음",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "SUBSECTION_NOT_FOUND",
                                        "message": "해당 서브 섹션이 존재하지 않습니다."
                                      }
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "UNAUTHORIZED_ACCESS",
                                        "message": "권한이 없습니다."
                                      }
                                    }
                                    """
                            )
                    )
            )
    })
    @DeleteMapping("/{planId}/subsections/{subSectionType}")
    ApiResponse<SubSectionResult.Result> deleteSubSection(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId,
            @PathVariable SubSectionType subSectionType
    );
}
