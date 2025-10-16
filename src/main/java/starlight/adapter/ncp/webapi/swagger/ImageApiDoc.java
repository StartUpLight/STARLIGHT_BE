package starlight.adapter.ncp.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.application.infrastructure.dto.PreSignedUrlResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "UTIL", description = "유틸리티 API")
public interface ImageApiDoc {

    @Operation(
            summary = "Presigned URL 발급",
            description = "S3 Presigned URL을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 Presigned URL 반환",
                    content = @Content(
                            schema = @Schema(implementation = PreSignedUrlResponse.class),
                            examples = @ExampleObject(
                                    name = "Presigned URL 예시",
                                    value = """
                                    {
                                      "result": "SUCCESS",
                                      "data": {
                                        "preSignedUrl": "https://starlight-s3.kr.object.ncloudstorage.com/test/..........",
                                        "objectUrl": "https://starlight-s3.kr.object.ncloudstorage.com/test/78e6919c-0b0f-47bd-96c6-2b3e9b176167-test"
                                      },
                                      "error": null
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping(value = "/v1/image/upload-url", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PreSignedUrlResponse> getPresignedUrl(
            @io.swagger.v3.oas.annotations.Parameter(description = "UserId", required = true) @RequestParam Long userId,
            @io.swagger.v3.oas.annotations.Parameter(description = "파일명", required = true) @RequestParam String fileName
    );

    @Operation(
            summary = "이미지 공개 전환",
            description = "업로드된 이미지를 공개 상태로 전환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 공개 처리",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "공개 처리 성공",
                                    value = """
                                    {
                                      "result": "SUCCESS",
                                      "data": "test/000239dc-542e-493a-aceb-6eda786d0eaf-tests",
                                      "error": null
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/v1/images/upload-url/public")
    ApiResponse<?> finalizePublic(
            @io.swagger.v3.oas.annotations.Parameter(description = "S3 Object URL", required = true) @RequestParam String objectUrl
    );
}

