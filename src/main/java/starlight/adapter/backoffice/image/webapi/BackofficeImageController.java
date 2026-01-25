package starlight.adapter.backoffice.image.webapi;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.aireport.infrastructure.storage.NcpPresignedUrlProvider;
import starlight.adapter.backoffice.image.webapi.dto.request.BackofficeImagePublicRequest;
import starlight.adapter.backoffice.image.webapi.swagger.BackofficeImageApiDoc;
import starlight.shared.apiPayload.response.ApiResponse;
import starlight.shared.dto.infrastructure.PreSignedUrlResponse;

@Validated
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
@RequestMapping("/v1/backoffice/images")
public class BackofficeImageController implements BackofficeImageApiDoc {

    private static final long BACKOFFICE_USER_ID = 0L;

    private final NcpPresignedUrlProvider presignedUrlProvider;

    @GetMapping(value = "/upload-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PreSignedUrlResponse> getPresignedUrl(
            @RequestParam String fileName
    ) {
        return ApiResponse.success(presignedUrlProvider.getPreSignedUrl(BACKOFFICE_USER_ID, fileName));
    }

    @PostMapping(value = "/upload-url/public", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<String> finalizePublic(
            @RequestBody BackofficeImagePublicRequest request
    ) {
        return ApiResponse.success(presignedUrlProvider.makePublic(request.objectUrl()));
    }
}
