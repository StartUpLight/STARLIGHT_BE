package starlight.adapter.ncp.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import starlight.adapter.ncp.webapi.dto.PreSignedUrlResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import starlight.application.infrastructure.provided.PresignedUrlProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcpPresignedUrlProvider implements PresignedUrlProvider {

    private final S3Client ncpS3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.ncp.object-storage.bucket-name}")
    private String bucket;
    @Value("${cloud.ncp.object-storage.end-point}")
    private String endpoint;

    /**
     * 업로드용 Presigned URL 생성
     * - 클라이언트는 추가 헤더 없이 PUT(binary)만 하면 됨
     */
    @Override
    public PreSignedUrlResponse getPreSignedUrl(Long userId, String originalFileName) {
        String safeFileName = encodePathSegment(originalFileName);
        String key = String.format("%d/%s", userId, safeFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        String presignedUrl = presignedRequest.url().toString();
        String objectUrl = buildObjectUrl(key);

        return PreSignedUrlResponse.of(presignedUrl, objectUrl);
    }

    /**
     * 업로드 후 공개가 필요할 때 서버에서 ACL을 지정
     */
    public String makePublic(String objectUrl) {
        String key = extractKeyFromUrl(objectUrl);

        try {
            PutObjectAclRequest aclRequest = PutObjectAclRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            ncpS3Client.putObjectAcl(aclRequest);
            log.info("객체 공개 처리 완료(PUBLIC_READ): key={}", objectUrl);
        } catch (S3Exception e) {
            log.error("객체 공개 처리 실패 - Message: {}", e.getMessage());
            throw new RuntimeException("객체 공개 처리 실패: " + e.getMessage(), e);
        }
        return objectUrl;
    }

    /**
     * 파일명/경로 segment-safe 인코딩 (공백을 %20으로 보존)
     */
    private static String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * 가상호스트 형태의 공개 URL 생성
     */
    private String buildObjectUrl(String key) {
        String host = endpoint;
        if (host.endsWith("/")) host = host.substring(0, host.length() - 1);

        return String.format("https://%s.kr.object.ncloudstorage.com/%s", bucket, key);
    }

    /**
     * Object URL에서 key 부분 추출
     */
    private String extractKeyFromUrl(String objectUrl) {
        int schemeEnd = objectUrl.indexOf("://");
        if (schemeEnd == -1) throw new IllegalArgumentException("잘못된 URL 형식");

        int pathStart = objectUrl.indexOf("/", schemeEnd + 3);
        if (pathStart == -1) throw new IllegalArgumentException("잘못된 URL 형식 - path가 없습니다");

        return objectUrl.substring(pathStart + 1);
    }
}
