package starlight.adapter.businessplan.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import starlight.shared.dto.infrastructure.PreSignedUrlResponse;
import starlight.application.aireport.required.PresignedUrlProviderPort;
import starlight.adapter.businessplan.webapi.swagger.BusinessPlanImageApiDoc;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequestMapping("/v1/business-plans/images")
@RequiredArgsConstructor
public class BusinessPlanBusinessPlanImageController implements BusinessPlanImageApiDoc {

    private final PresignedUrlProviderPort presignedUrlReader;

    @GetMapping(value = "/upload-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PreSignedUrlResponse> getPresignedUrl(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @RequestParam String fileName
    ) {
        return ApiResponse.success(presignedUrlReader.getPreSignedUrl(authenticatedMember.getMemberId(), fileName));
    }

    @PostMapping("/upload-url/public")
    public ApiResponse<String> finalizePublic(@RequestParam String objectUrl) {
        return ApiResponse.success(presignedUrlReader.makePublic(objectUrl));
    }
}
