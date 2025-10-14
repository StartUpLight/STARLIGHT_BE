package starlight.application.infrastructure.provided;

import starlight.adapter.ncp.webapi.dto.PreSignedUrlResponse;

public interface PresignedUrlProvider {

    PreSignedUrlResponse getPreSignedUrl(Long userId, String originalFileName);

    String makePublic(String objectUrl);
}
