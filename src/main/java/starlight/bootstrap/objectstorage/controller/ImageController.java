package starlight.bootstrap.objectstorage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starlight.bootstrap.objectstorage.PreSignedUrlResponse;
import starlight.bootstrap.objectstorage.PresignedUrlReader;
import starlight.shared.apiPayload.response.ApiResponse;

@Slf4j
@RestController
@RequestMapping("/v1/image")
@RequiredArgsConstructor
@Tag(name = "UTIL", description = "이미지 관련 API")
public class ImageController {

    private final PresignedUrlReader presignedUrlReader;

    @GetMapping(value = "/presigned-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PreSignedUrlResponse> getPresignedUrl(@RequestParam String prefix, @RequestParam String fileName) {
        return ApiResponse.success(presignedUrlReader.getPreSignedUrl(prefix, fileName));
    }

    @PostMapping("/finalize-public")
    public ApiResponse<?> finalizePublic(@RequestParam String objectUrl) {
        return ApiResponse.success(presignedUrlReader.makePublic(objectUrl));
    }
}
