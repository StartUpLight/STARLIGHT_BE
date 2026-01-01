package starlight.application.aireport.required;

import starlight.shared.dto.infrastructure.PreSignedUrlResponse;

public interface PresignedUrlProvider {

    PreSignedUrlResponse getPreSignedUrl(Long userId, String originalFileName);

    String makePublic(String objectUrl);
}
