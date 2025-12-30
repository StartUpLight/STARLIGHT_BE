package starlight.adapter.aireport.infrastructure.ocr.infra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import starlight.adapter.aireport.infrastructure.ocr.dto.ClovaOcrRequest;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrErrorType;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrException;
import starlight.adapter.aireport.infrastructure.ocr.infra.ClovaOcrClient;
import starlight.shared.dto.infrastructure.OcrResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClovaOcrClient 테스트")
class ClovaOcrClientTest {

    @Mock private RestClient clovaOcrRestClient;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private ClovaOcrClient clovaOcrClient;
    private byte[] testPdfBytes;

    @BeforeEach
    void setUp() {
        clovaOcrClient = new ClovaOcrClient(clovaOcrRestClient);
        testPdfBytes = "test pdf content".getBytes();
    }

    @Test
    @DisplayName("PDF 바이트를 전달하면 OCR 응답을 반환한다")
    void recognizePdfBytes_Success() {
        // given
        OcrResponse expectedResponse = OcrResponse.createEmpty();

        when(clovaOcrRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(ArgumentMatchers.<ClovaOcrRequest>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(OcrResponse.class)).thenReturn(expectedResponse);

        // when
        OcrResponse result = clovaOcrClient.recognizePdfBytes(testPdfBytes);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("OCR 호출 중 예외 발생 시 OcrException을 던진다")
    void recognizePdfBytes_ThrowsOcrException_WhenClientFails() {
        // given
        when(clovaOcrRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(ArgumentMatchers.<ClovaOcrRequest>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(OcrResponse.class)).thenThrow(new RuntimeException("Network error"));

        // when & then
        assertThatThrownBy(() -> clovaOcrClient.recognizePdfBytes(testPdfBytes))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.OCR_CLIENT_ERROR);
    }

    @Test
    @DisplayName("빈 PDF 바이트 배열로도 정상 호출된다")
    void recognizePdfBytes_WithEmptyBytes() {
        // given
        byte[] emptyBytes = new byte[0];
        OcrResponse expectedResponse = OcrResponse.createEmpty();

        when(clovaOcrRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(ArgumentMatchers.<ClovaOcrRequest>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(OcrResponse.class)).thenReturn(expectedResponse);

        // when
        OcrResponse result = clovaOcrClient.recognizePdfBytes(emptyBytes);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("응답이 null인 경우 예외가 발생한다")
    void recognizePdfBytes_WithNullResponse() {
        // given
        when(clovaOcrRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(ArgumentMatchers.<ClovaOcrRequest>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(OcrResponse.class)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> clovaOcrClient.recognizePdfBytes(testPdfBytes))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.OCR_CLIENT_ERROR);
    }

    @Test
    @DisplayName("retrieve 단계에서 예외 발생 시 OcrException 발생")
    void recognizePdfBytes_ThrowsOcrException_WhenRetrieveFails() {
        // given
        when(clovaOcrRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(ArgumentMatchers.<ClovaOcrRequest>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Connection timeout"));

        // when & then
        assertThatThrownBy(() -> clovaOcrClient.recognizePdfBytes(testPdfBytes))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.OCR_CLIENT_ERROR);
    }

    @Test
    @DisplayName("body 파싱 중 예외 발생 시 OcrException 발생")
    void recognizePdfBytes_ThrowsOcrException_WhenBodyParseFails() {
        // given
        when(clovaOcrRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(ArgumentMatchers.<ClovaOcrRequest>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(OcrResponse.class)).thenThrow(new RuntimeException("JSON parse error"));

        // when & then
        assertThatThrownBy(() -> clovaOcrClient.recognizePdfBytes(testPdfBytes))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.OCR_CLIENT_ERROR);
    }
}