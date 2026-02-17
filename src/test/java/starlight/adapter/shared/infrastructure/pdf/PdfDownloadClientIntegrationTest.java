package starlight.adapter.shared.infrastructure.pdf;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrErrorType;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrException;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PdfDownloadClient 통합 테스트 (MockWebServer)")
class PdfDownloadClientIntegrationTest {

    private MockWebServer mockWebServer;
    private PdfDownloadClient pdfDownloadClient;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("/").toString();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(5));

        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "TEST-CLIENT/1.0")
                .build();

        pdfDownloadClient = new PdfDownloadClient(restClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("실제 HTTP 요청으로 PDF 다운로드 성공")
    void downloadFromUrl_RealHttpRequest_Success() throws InterruptedException {
        // given
        byte[] expectedBytes = "PDF content".getBytes();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new String(expectedBytes))
                .addHeader("Content-Type", "application/pdf"));

        String url = baseUrl + "test.pdf";

        // when
        byte[] result = pdfDownloadClient.downloadFromUrl(url);

        // then
        assertThat(result).isEqualTo(expectedBytes);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/test.pdf");
        assertThat(request.getHeader("User-Agent")).isEqualTo("TEST-CLIENT/1.0");
    }

    @Test
    @DisplayName("404 응답 시 PDF_DOWNLOAD_ERROR 예외 발생")
    void downloadFromUrl_Returns404_ThrowsException() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        String url = baseUrl + "notfound.pdf";

        // when & then
        assertThatThrownBy(() -> pdfDownloadClient.downloadFromUrl(url))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_DOWNLOAD_ERROR);
    }

    @Test
    @DisplayName("500 응답 시 PDF_DOWNLOAD_ERROR 예외 발생")
    void downloadFromUrl_Returns500_ThrowsException() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        String url = baseUrl + "error.pdf";

        // when & then
        assertThatThrownBy(() -> pdfDownloadClient.downloadFromUrl(url))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_DOWNLOAD_ERROR);
    }

    @Test
    @DisplayName("빈 응답 본문인 경우 PDF_EMPTY_RESPONSE 예외 발생")
    void downloadFromUrl_EmptyBody_ThrowsException() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(""));

        String url = baseUrl + "empty.pdf";

        // when & then
        assertThatThrownBy(() -> pdfDownloadClient.downloadFromUrl(url))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_EMPTY_RESPONSE);
    }

    @Test
    @DisplayName("쿼리 파라미터가 포함된 URL 처리")
    void downloadFromUrl_WithQueryParams_Success() throws InterruptedException {
        // given
        byte[] expectedBytes = "PDF with params".getBytes();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new String(expectedBytes)));

        String url = baseUrl + "test.pdf?token=abc123&expires=2025-12-31";

        // when
        byte[] result = pdfDownloadClient.downloadFromUrl(url);

        // then
        assertThat(result).isEqualTo(expectedBytes);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("token=abc123");
        assertThat(request.getPath()).contains("expires=2025-12-31");
    }

    @Test
    @DisplayName("큰 PDF 파일 다운로드 성공 (10MB)")
    void downloadFromUrl_LargeFile_Success() {
        // given
        byte[] largeBytes = new byte[10 * 1024 * 1024]; // 10MB
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new String(largeBytes)));

        String url = baseUrl + "large.pdf";

        // when
        byte[] result = pdfDownloadClient.downloadFromUrl(url);

        // then
        assertThat(result).hasSize(10 * 1024 * 1024);
    }
}