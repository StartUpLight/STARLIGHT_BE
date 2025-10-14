package starlight.adapter.ncp.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starlight.application.infrastructure.dto.PreSignedUrlResponse;
import starlight.application.infrastructure.provided.PresignedUrlProvider;
import starlight.adapter.ncp.webapi.swagger.ImageApiDoc;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequestMapping("/v1/image")
@RequiredArgsConstructor
public class ImageController implements ImageApiDoc {

    private final PresignedUrlProvider presignedUrlReader;

    @GetMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PreSignedUrlResponse> getPresignedUrl(@RequestParam Long userId, @RequestParam String fileName) {
        return ApiResponse.success(presignedUrlReader.getPreSignedUrl(userId, fileName));
    }

    @PostMapping("/upload/public")
    public ApiResponse<?> finalizePublic(@RequestParam String objectUrl) {
        return ApiResponse.success(presignedUrlReader.makePublic(objectUrl));
    }
}
