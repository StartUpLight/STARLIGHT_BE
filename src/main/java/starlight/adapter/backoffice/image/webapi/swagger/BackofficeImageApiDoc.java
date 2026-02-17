package starlight.adapter.backoffice.image.webapi.swagger;

import jakarta.validation.Valid;
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
import starlight.adapter.backoffice.image.webapi.validation.ValidImageFileName;
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
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "fileName 검증 실패",
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
                            ),
                            @ExampleObject(
                                    name = "요청 값 오류",
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
                            )
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Presigned URL 생성 실패",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INTERNAL_ERROR",
                                "message": "알 수 없는 내부 오류입니다."
                              }
                            }
                            """
                    ))
            )
    })
    @GetMapping(value = "/v1/backoffice/images/upload-url", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PreSignedUrlResponse> getPresignedUrl(
            @RequestParam @ValidImageFileName String fileName
    );

    @Operation(
            summary = "이미지 공개 전환(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "result": "SUCCESS",
                                      "data": "https://bucket.example.com/path/to/object.jpg",
                                      "error": null
                                    }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "요청 값 오류",
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
                            ),
                            @ExampleObject(
                                    name = "JSON 형식 오류",
                                    value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "INVALID_REQUEST_ARGUMENT",
                                        "message": "잘못된 JSON 형식입니다."
                                      }
                                    }
                                    """
                            )
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "이미지 공개 처리 실패",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INTERNAL_ERROR",
                                "message": "알 수 없는 내부 오류입니다."
                              }
                            }
                            """
                    ))
            )
    })
    @PostMapping(value = "/v1/backoffice/images/upload-url/public", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> finalizePublic(
            @Valid @RequestBody BackofficeImagePublicRequest request
    );
}
