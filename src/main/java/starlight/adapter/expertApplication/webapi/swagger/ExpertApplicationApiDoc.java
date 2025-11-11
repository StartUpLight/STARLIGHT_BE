package starlight.adapter.expertApplication.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import starlight.adapter.auth.security.auth.AuthDetails;

import java.util.List;

@Tag(name = "전문가", description = "전문가 관련 API")
public interface ExpertApplicationApiDoc {

    @Operation(
            summary = "피드백 요청한 전문가 목록 조회",
            description = "특정 사업계획서에 피드백을 요청한 전문가들의 ID 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                            "result": "SUCCESS",
                            "data": [1, 3, 5, 7],
                            "error": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사업계획서를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                            "result": "ERROR",
                            "data": null,
                            "error": {
                                "code": "BUSINESS_PLAN_NOT_FOUND",
                                "message": "사업계획서를 찾을 수 없습니다."
                            }
                        }
                        """
                            )
                    )
            )
    })
    starlight.shared.apiPayload.response.ApiResponse<List<Long>> search(
            @Parameter(
                    description = "사업계획서 ID",
                    required = true,
                    example = "1"
            )
            @RequestParam Long businessPlanId
    );

    @Operation(
            summary = "전문가에게 피드백 요청",
            description = """
            특정 전문가에게 사업계획서에 대한 피드백을 요청합니다.
            
            - 사업계획서 PDF 파일을 첨부하여 전문가 이메일로 발송합니다.
            - 동일한 전문가에게 동일한 사업계획서로 중복 요청할 수 없습니다.
            - 이메일 발송은 비동기로 처리되며, 요청 즉시 응답을 반환합니다.
            """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
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
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (파일 없음, 파일 형식 오류 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                            "result": "ERROR",
                            "data": null,
                            "error": {
                                "code": "INVALID_FILE",
                                "message": "유효하지 않은 파일입니다."
                            }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
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
                                    "message": "사업계획서를 찾을 수 없습니다."
                                }
                            }
                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
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
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (파일 처리 실패, 이메일 발송 실패 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                            "result": "ERROR",
                            "data": null,
                            "error": {
                                "code": "INTERNAL_SERVER_ERROR",
                                "message": "서버 오류가 발생했습니다."
                            }
                        }
                        """
                            )
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
    starlight.shared.apiPayload.response.ApiResponse<String> requestFeedback(
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
            @AuthenticationPrincipal AuthDetails auth
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