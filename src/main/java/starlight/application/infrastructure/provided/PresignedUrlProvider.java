package starlight.application.infrastructure.provided;

import starlight.application.infrastructure.dto.PreSignedUrlResponse;

public interface PresignedUrlProvider {

    PreSignedUrlResponse getPreSignedUrl(Long userId, String originalFileName);

    String makePublic(String objectUrl);
}
