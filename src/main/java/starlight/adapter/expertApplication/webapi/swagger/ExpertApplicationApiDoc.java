package starlight.adapter.expertApplication.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "전문가", description = "전문가 관련 API")
public interface ExpertApplicationApiDoc {

    @Operation(
            summary = "전문가에게 피드백 요청",
            description = """
            특정 전문가에게 사업계획서에 대한 피드백을 요청합니다.
            
            - 사업계획서 PDF 파일을 첨부하여 전문가 이메일로 발송합니다.
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
                    description = "잘못된 요청 (파일 없음)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "빈 파일",
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EMPTY_FILE",
                            "message": "업로드할 파일이 비어 있습니다."
                          }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "파일 크기 초과",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "FILE_SIZE_EXCEEDED",
                            "message": "파일 크기는 최대 20MB까지 업로드 가능합니다."
                          }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "415",
                    description = "지원하지 않는 파일 형식",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "UNSUPPORTED_FILE_TYPE",
                            "message": "지원되지 않는 파일 형식입니다."
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
                    description = "서버 오류 (파일 처리 실패 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "파일 읽기 실패",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "FILE_READ_ERROR",
                                "message": "파일을 읽는 중에 오류가 발생했습니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
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
                            }
                    )
            )
    })
    @RequestBody(
            description = "피드백 요청 정보",
            required = true,
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(implementation = FeedbackRequestSchema.class)
            )
    )
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
                    description = "사업계획서 PDF 파일 (최대 50MB)",
                    required = true,
                    content = @Content(mediaType = "application/pdf")
            )
            @RequestParam("file") MultipartFile file,

            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) throws Exception;

    /**
     * Swagger 문서화를 위한 스키마 클래스
     */
    @Schema(description = "피드백 요청 데이터")
    class FeedbackRequestSchema {

        @Schema(
                description = "사업계획서 ID",
                example = "10",
                required = true
        )
        public Long businessPlanId;

        @Schema(
                description = "사업계획서 PDF 파일",
                type = "string",
                format = "binary",
                required = true,
                maxLength = 52428800  // 50MB
        )
        public MultipartFile file;
    }
}
