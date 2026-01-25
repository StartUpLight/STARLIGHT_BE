package starlight.adapter.backoffice.image.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.adapter.backoffice.image.webapi.dto.request.BackofficeImagePublicRequest;
import starlight.shared.apiPayload.response.ApiResponse;
import starlight.shared.dto.infrastructure.PreSignedUrlResponse;

@Tag(name = "[Office] 이미지", description = "백오피스 이미지 업로드 API")
public interface BackofficeImageApiDoc {

    @Operation(
            summary = "Presigned URL 발급(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = PreSignedUrlResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "fileName 검증 실패",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_REQUEST_ARGUMENT",
                                "message": "fileName이 올바르지 않습니다."
                              }
                            }
                            """
                    ))
            )
    })
    @GetMapping(value = "/v1/backoffice/images/upload-url", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PreSignedUrlResponse> getPresignedUrl(
            @RequestParam String fileName
    );

    @Operation(
            summary = "이미지 공개 전환(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
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
    @PostMapping(value = "/v1/backoffice/images/upload-url/public", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> finalizePublic(
            @RequestBody BackofficeImagePublicRequest request
    );
}
