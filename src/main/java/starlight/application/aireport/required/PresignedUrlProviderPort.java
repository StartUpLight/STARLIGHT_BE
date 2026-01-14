package starlight.application.aireport.required;

import starlight.shared.dto.infrastructure.PreSignedUrlResponse;

public interface PresignedUrlProviderPort {

    PreSignedUrlResponse getPreSignedUrl(Long userId, String originalFileName);

    String makePublic(String objectUrl);
}
