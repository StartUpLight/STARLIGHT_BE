package starlight.adapter.aireport.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.aireport.webapi.dto.AiReportCreateWithPdfRequest;
import starlight.adapter.aireport.webapi.dto.AiReportResponse;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "AI 리포트", description = "AI 리포트 채점 및 조회 API")
@SecurityRequirement(name = "bearerAuth")
public interface AiReportApiDoc {

    @Operation(
            summary = "사업계획서를 AI로 채점 및 생성합니다.",
            description = "작성 완료된 사업계획서를 AI로 채점하여 리포트를 생성합니다. 기존 리포트가 있으면 업데이트합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AiReportResponse.class)
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
                                        "code": "NOT_READY_FOR_AI_REPORT",
                                        "message": "사업계획서가 작성 완료되지 않아 AI 리포트를 생성할 수 없습니다."
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
                    responseCode = "500",
                    description = "AI 채점 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "AI 채점 실패",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "AI_GRADING_FAILED",
                                        "message": "AI 채점에 실패했습니다."
                                      }
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AI 응답 파싱 실패",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "AI_RESPONSE_PARSING_FAILED",
                                        "message": "AI 응답 파싱에 실패했습니다."
                                      }
                                    }
                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/evaluation/{planId}")
    ApiResponse<AiReportResponse> gradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    );

    @Operation(
            summary = "PDF URL을 기반으로 사업계획서를 생성하고, AI로 채점 및 생성합니다.",
            description = "PDF URL을 제공하여 사업계획서를 생성하고, OCR로 텍스트를 추출한 후 AI로 채점하여 리포트를 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AiReportResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "요청 데이터 오류",
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
                                    ),
                                    @ExampleObject(
                                            name = "OCR 실패",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "OCR_FAILED",
                                        "message": "PDF에서 텍스트를 추출하는데 실패했습니다."
                                      }
                                    }
                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "AI 채점 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "AI 채점 실패",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "AI_GRADING_FAILED",
                                        "message": "AI 채점에 실패했습니다."
                                      }
                                    }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AI 응답 파싱 실패",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "AI_RESPONSE_PARSING_FAILED",
                                        "message": "AI 응답 파싱에 실패했습니다."
                                      }
                                    }
                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/evaluation/pdf")
    ApiResponse<AiReportResponse> createAndGradeBusinessPlan(
            @AuthenticationPrincipal AuthDetails authDetails,
            @Valid @RequestBody AiReportCreateWithPdfRequest request
    );

    @Operation(
            summary = "AI 리포트를 조회합니다.",
            description = "지정된 사업계획서의 AI 리포트를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AiReportResponse.class)
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
                                            name = "AI 리포트 없음",
                                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "AI_REPORT_NOT_FOUND",
                                        "message": "해당 AI 리포트가 존재하지 않습니다."
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
    @GetMapping("/{planId}")
    ApiResponse<AiReportResponse> getAiReport(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Long planId
    );
}
