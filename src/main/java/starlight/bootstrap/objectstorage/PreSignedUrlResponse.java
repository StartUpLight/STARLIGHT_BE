package starlight.bootstrap.objectstorage;

import lombok.Builder;

@Builder
public record PreSignedUrlResponse (
        String preSignedUrl,

        String objectUrl
){
    public static PreSignedUrlResponse of(String preSignedUrl, String objectUrl) {
        return PreSignedUrlResponse.builder()
                .preSignedUrl(preSignedUrl)
                .objectUrl(objectUrl)
                .build();
    }
}