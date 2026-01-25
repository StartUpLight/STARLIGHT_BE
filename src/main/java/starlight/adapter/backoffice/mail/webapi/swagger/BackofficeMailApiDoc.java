package starlight.adapter.backoffice.mail.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import starlight.adapter.backoffice.mail.webapi.dto.request.BackofficeMailSendRequest;
import starlight.adapter.backoffice.mail.webapi.dto.request.BackofficeMailTemplateCreateRequest;
import starlight.adapter.backoffice.mail.webapi.dto.response.BackofficeMailTemplateResponse;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Tag(name = "[Office] 메일", description = "백오피스 메일 관리 API")
public interface BackofficeMailApiDoc {

    @Operation(
            summary = "백오피스 메일 발송",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "SUCCESS",
                              "data": "이메일 전송에 성공하였습니다.",
                              "error": null
                            }
                            """
                    ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_REQUEST_ARGUMENT",
                                "message": "잘못된 요청 인자입니다."
                              }
                            }
                            """
                    ))
            )
    })
    @PostMapping("/v1/backoffice/mail/send")
    ApiResponse<String> send(
            @RequestBody BackofficeMailSendRequest request
    );

    @Operation(
            summary = "메일 템플릿 생성",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = BackofficeMailTemplateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_REQUEST_ARGUMENT",
                                "message": "잘못된 요청 인자입니다."
                              }
                            }
                            """
                    ))
            )
    })
    @PostMapping("/v1/backoffice/mail/templates")
    ApiResponse<BackofficeMailTemplateResponse> createTemplate(
            @RequestBody BackofficeMailTemplateCreateRequest request
    );

    @Operation(
            summary = "메일 템플릿 목록 조회",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BackofficeMailTemplateResponse.class)))
            )
    })
    @GetMapping("/v1/backoffice/mail/templates")
    ApiResponse<List<BackofficeMailTemplateResponse>> findTemplates();

    @Operation(
            summary = "메일 템플릿 삭제",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "SUCCESS",
                              "data": "템플릿이 삭제되었습니다.",
                              "error": null
                            }
                            """
                    ))
            )
    })
    @DeleteMapping("/v1/backoffice/mail/templates/{templateId}")
    ApiResponse<String> deleteTemplate(
            @PathVariable Long templateId
    );
}
