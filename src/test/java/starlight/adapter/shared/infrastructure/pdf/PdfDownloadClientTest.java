package starlight.adapter.shared.infrastructure.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import starlight.adapter.shared.infrastructure.pdf.exception.PdfDownloadErrorType;
import starlight.adapter.shared.infrastructure.pdf.exception.PdfDownloadException;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PdfDownloadClient 테스트")
class PdfDownloadClientTest {

    private RestClient pdfDownloadClient;
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.ResponseSpec responseSpec;
    private PdfDownloadClient pdfDownloadClientInstance;

    private static final String TEST_URL = "https://example.com/test.pdf";
    private byte[] testPdfBytes;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        pdfDownloadClient = mock(RestClient.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        pdfDownloadClientInstance = new PdfDownloadClient(pdfDownloadClient);
        testPdfBytes = createPdfBytes(1024); // 1KB
    }

    @Test
    @DisplayName("정상적인 PDF 다운로드 성공")
    void downloadFromUrl_Success() {
        // given
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok(testPdfBytes);

        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(responseEntity);

        // when
        byte[] result = pdfDownloadClientInstance.downloadFromUrl(TEST_URL);

        // then
        assertThat(result).isEqualTo(testPdfBytes);
        verify(pdfDownloadClient).get();
        verify(requestHeadersUriSpec).uri(URI.create(TEST_URL));
    }

    @Test
    @DisplayName("빈 응답인 경우 PDF_EMPTY_RESPONSE 예외 발생")
    void downloadFromUrl_ThrowsException_WhenResponseIsEmpty() {
        // given
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok(new byte[0]);

        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> pdfDownloadClientInstance.downloadFromUrl(TEST_URL))
                .isInstanceOf(PdfDownloadException.class)
                .hasFieldOrPropertyWithValue("errorType", PdfDownloadErrorType.PDF_EMPTY_RESPONSE);
    }

    @Test
    @DisplayName("응답 Body가 null인 경우 PDF_EMPTY_RESPONSE 예외 발생")
    void downloadFromUrl_ThrowsException_WhenResponseBodyIsNull() {
        // given
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok().build();

        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> pdfDownloadClientInstance.downloadFromUrl(TEST_URL))
                .isInstanceOf(PdfDownloadException.class)
                .hasFieldOrPropertyWithValue("errorType", PdfDownloadErrorType.PDF_EMPTY_RESPONSE);
    }

    @Test
    @DisplayName("PDF 크기가 30MB를 초과하면 PDF_TOO_LARGE 예외 발생")
    void downloadFromUrl_ThrowsException_WhenPdfIsTooLarge() {
        // given
        byte[] largePdfBytes = createPdfBytes(31 * 1024 * 1024); // 31MB
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok(largePdfBytes);

        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> pdfDownloadClientInstance.downloadFromUrl(TEST_URL))
                .isInstanceOf(PdfDownloadException.class)
                .hasFieldOrPropertyWithValue("errorType", PdfDownloadErrorType.PDF_TOO_LARGE);
    }

    @Test
    @DisplayName("정확히 30MB인 PDF는 정상 다운로드")
    void downloadFromUrl_Success_WhenPdfIsExactly30MB() {
        // given
        byte[] exactSizePdfBytes = createPdfBytes(30 * 1024 * 1024); // 정확히 30MB
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok(exactSizePdfBytes);

        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(responseEntity);

        // when
        byte[] result = pdfDownloadClientInstance.downloadFromUrl(TEST_URL);

        // then
        assertThat(result).isEqualTo(exactSizePdfBytes);
    }

    @Test
    @DisplayName("네트워크 예외 발생 시 PDF_DOWNLOAD_ERROR 예외 발생")
    void downloadFromUrl_ThrowsException_WhenNetworkError() {
        // given
        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenThrow(new RuntimeException("Network error"));

        // when & then
        assertThatThrownBy(() -> pdfDownloadClientInstance.downloadFromUrl(TEST_URL))
                .isInstanceOf(PdfDownloadException.class)
                .hasFieldOrPropertyWithValue("errorType", PdfDownloadErrorType.PDF_DOWNLOAD_ERROR);
    }

    @Test
    @DisplayName("특수문자가 포함된 URL도 정상 처리")
    void downloadFromUrl_Success_WithEncodedUrl() {
        // given
        String encodedUrl = "https://example.com/test%20file.pdf?param=value&signed=abc123";
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok(testPdfBytes);

        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(responseEntity);

        // when
        byte[] result = pdfDownloadClientInstance.downloadFromUrl(encodedUrl);

        // then
        assertThat(result).isEqualTo(testPdfBytes);
        verify(requestHeadersUriSpec).uri(URI.create(encodedUrl));
    }

    @Test
    @DisplayName("프리사인드 URL도 정상 처리")
    void downloadFromUrl_Success_WithPresignedUrl() {
        // given
        String presignedUrl = "https://s3.amazonaws.com/bucket/file.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=xxx";
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok(testPdfBytes);

        when(pdfDownloadClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(responseEntity);

        // when
        byte[] result = pdfDownloadClientInstance.downloadFromUrl(presignedUrl);

        // then
        assertThat(result).isEqualTo(testPdfBytes);
    }

    // 헬퍼 메서드
    private byte[] createPdfBytes(int size) {
        return new byte[size];
    }
}