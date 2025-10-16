package starlight.adapter.ncp.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import starlight.application.infrastructure.dto.PreSignedUrlResponse;

import java.net.URL;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NcpPresignedUrlProvider 단위 테스트")
class NcpPresignedUrlProviderUnitTest {

    @Mock
    private S3Client ncpS3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private NcpPresignedUrlProvider presignedUrlProvider;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String ENDPOINT = "https://kr.object.ncloudstorage.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(presignedUrlProvider, "bucket", BUCKET_NAME);
        ReflectionTestUtils.setField(presignedUrlProvider, "endpoint", ENDPOINT);
    }

    @Test
    @DisplayName("Presigned URL 생성 성공")
    void getPreSignedUrl_Success() throws Exception {
        // given
        Long userId = 1L;
        String fileName = "test-image.jpg";
        String expectedKey = "1/test-image.jpg";
        URL mockUrl = new URL("https://test-bucket.kr.object.ncloudstorage.com/presigned-url");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        given(mockPresignedRequest.url()).willReturn(mockUrl);
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(mockPresignedRequest);

        // when
        PreSignedUrlResponse response = presignedUrlProvider.getPreSignedUrl(userId, fileName);

        // then
        assertThat(response).isNotNull();
        assertThat(response.preSignedUrl()).contains("presigned-url");
        assertThat(response.objectUrl()).contains(BUCKET_NAME);
        assertThat(response.objectUrl()).contains(expectedKey);

        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("파일명에 공백이 있을 때 URL 인코딩 처리")
    void getPreSignedUrl_WithSpaceInFileName() throws Exception {
        // given
        Long userId = 1L;
        String fileName = "test image.jpg";
        URL mockUrl = new URL("https://test-bucket.kr.object.ncloudstorage.com/presigned-url");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        given(mockPresignedRequest.url()).willReturn(mockUrl);
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(mockPresignedRequest);

        // when
        PreSignedUrlResponse response = presignedUrlProvider.getPreSignedUrl(userId, fileName);

        // then
        assertThat(response.objectUrl()).contains("test%20image.jpg");
        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("파일명에 특수문자가 있을 때 URL 인코딩 처리")
    void getPreSignedUrl_WithSpecialCharacters() throws Exception {
        // given
        Long userId = 1L;
        String fileName = "test#파일.jpg";
        URL mockUrl = new URL("https://test-bucket.kr.object.ncloudstorage.com/presigned-url");

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        given(mockPresignedRequest.url()).willReturn(mockUrl);
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(mockPresignedRequest);

        // when
        PreSignedUrlResponse response = presignedUrlProvider.getPreSignedUrl(userId, fileName);

        // then
        assertThat(response.objectUrl()).doesNotContain("#");
        assertThat(response.objectUrl()).contains("%");
    }

    @Test
    @DisplayName("객체 공개 처리 성공")
    void makePublic_Success() {
        // given
        String objectUrl = "https://test-bucket.kr.object.ncloudstorage.com/1/test-image.jpg";

        given(ncpS3Client.putObjectAcl(any(PutObjectAclRequest.class)))
                .willReturn(PutObjectAclResponse.builder().build());

        // when
        String result = presignedUrlProvider.makePublic(objectUrl);

        // then
        assertThat(result).isEqualTo(objectUrl);

        // ArgumentCaptor를 사용한 상세 검증
        ArgumentCaptor<PutObjectAclRequest> captor = ArgumentCaptor.forClass(PutObjectAclRequest.class);
        verify(ncpS3Client).putObjectAcl(captor.capture());

        PutObjectAclRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(capturedRequest.key()).isEqualTo("1/test-image.jpg");
        assertThat(capturedRequest.acl()).isEqualTo(ObjectCannedACL.PUBLIC_READ);
    }

    @Test
    @DisplayName("객체 공개 처리 실패 - S3Exception")
    void makePublic_Failure_S3Exception() {
        // given
        String objectUrl = "https://test-bucket.kr.object.ncloudstorage.com/1/test-image.jpg";

        given(ncpS3Client.putObjectAcl(any(PutObjectAclRequest.class)))
                .willThrow(S3Exception.builder().message("Access Denied").build());

        // when & then
        assertThatThrownBy(() -> presignedUrlProvider.makePublic(objectUrl))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("객체 공개 처리 실패");

        verify(ncpS3Client).putObjectAcl(any(PutObjectAclRequest.class));
    }

    @Test
    @DisplayName("잘못된 URL 형식 - 스킴 없음")
    void makePublic_InvalidUrl_NoScheme() {
        // given
        String invalidUrl = "test-bucket.kr.object.ncloudstorage.com/1/test-image.jpg";

        // when & then
        assertThatThrownBy(() -> presignedUrlProvider.makePublic(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 URL 형식");
    }

    @Test
    @DisplayName("잘못된 URL 형식 - 경로 없음")
    void makePublic_InvalidUrl_NoPath() {
        // given
        String invalidUrl = "https://test-bucket.kr.object.ncloudstorage.com";

        // when & then
        assertThatThrownBy(() -> presignedUrlProvider.makePublic(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path가 없습니다");
    }
}