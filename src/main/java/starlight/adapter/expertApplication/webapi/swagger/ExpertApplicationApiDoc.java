package starlight.adapter.expertApplication.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "전문가", description = "전문가 관련 API")
public interface ExpertApplicationApiDoc {

    @Operation(
            summary = "전문가에게 피드백 요청",
            description = """
            특정 전문가에게 사업계획서에 대한 피드백을 요청합니다.
            
            - 사업계획서 PDF URL을 전문가 이메일의 사업계획서 보기 링크로 전달합니다.
            - `pdfUrl`이 없으면 PDF 기반 사업계획서의 기존 URL을 사용합니다.
            - 동일한 전문가에게 동일한 사업계획서로 중복 요청할 수 없습니다.
            - 이메일 발송은 비동기로 처리되며, 요청 즉시 응답을 반환합니다.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드백 요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                            "result": "SUCCESS",
                            "data": "피드백 요청이 전달되었습니다.",
                            "error": null
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "유효하지 않은 PDF URL",
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "INVALID_PDF_URL",
                            "message": "사업계획서 PDF URL이 올바르지 않습니다."
                          }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 또는 사업계획서를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "전문가를 찾을 수 없음",
                                            value = """
                            {
                                "result": "ERROR",
                                "data": null,
                                "error": {
                                    "code": "EXPERT_NOT_FOUND",
                                    "message": "전문가를 찾을 수 없습니다."
                                }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "사업계획서를 찾을 수 없음",
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
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 피드백을 요청한 전문가",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                            "result": "ERROR",
                            "data": null,
                            "error": {
                                "code": "APPLICATION_ALREADY_EXISTS",
                                "message": "이미 신청한 전문가입니다."
                            }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "피드백 요청 처리 실패",
                                    value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "EXPERT_FEEDBACK_REQUEST_FAILED",
                                "message": "전문가 피드백 요청에 실패했습니다."
                              }
                            }
                            """
                            )
                    )
            )
    })
    ApiResponse<String> requestFeedback(
            @Parameter(
                    description = "전문가 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long expertId,

            @Parameter(
                    description = "사업계획서 ID",
                    required = true,
                    example = "10"
            )
            @RequestParam Long businessPlanId,

            @Parameter(
                    description = "사업계획서 PDF URL. 없으면 PDF 기반 사업계획서의 기존 URL을 사용합니다.",
                    required = false,
                    example = "https://kr.object.ncloudstorage.com/starlight-s3/business-plan.pdf"
            )
            @RequestParam(required = false) String pdfUrl,

            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    );
}
