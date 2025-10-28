package starlight.adapter.ncp.ocr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import starlight.adapter.ncp.ocr.exception.OcrErrorType;
import starlight.adapter.ncp.ocr.exception.OcrException;
import starlight.adapter.ncp.ocr.infra.ClovaOcrClient;
import starlight.adapter.ncp.ocr.infra.PdfDownloadClient;
import starlight.adapter.ncp.ocr.util.OcrResponseMerger;
import starlight.adapter.ncp.ocr.util.OcrTextExtractor;
import starlight.adapter.ncp.ocr.util.PdfUtils;
import starlight.shared.dto.infrastructure.ClovaOcrResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClovaOcrProvider 테스트")
class ClovaOcrProviderTest {

    @Mock
    private ClovaOcrClient clovaOcrClient;

    @Mock
    private PdfDownloadClient pdfDownloadClient;

    @InjectMocks
    private ClovaOcrProvider clovaOcrProvider;

    private static final String TEST_PDF_URL = "https://example.com/test.pdf";
    private byte[] testPdfBytes;
    private ClovaOcrResponse mockResponse1;
    private ClovaOcrResponse mockResponse2;

    @BeforeEach
    void setUp() {
        testPdfBytes = "test pdf content".getBytes();
        mockResponse1 = ClovaOcrResponse.createEmpty();
        mockResponse2 = ClovaOcrResponse.createEmpty();
    }

    @Test
    @DisplayName("단일 청크 PDF OCR 처리 성공")
    void ocrPdfByUrl_Success_SingleChunk() {
        // given
        byte[] chunk = "chunk1".getBytes();
        ClovaOcrResponse expectedResponse = ClovaOcrResponse.createEmpty();

        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL)).thenReturn(testPdfBytes);

        try (MockedStatic<PdfUtils> pdfUtilsMock = mockStatic(PdfUtils.class);
             MockedStatic<OcrResponseMerger> mergerMock = mockStatic(OcrResponseMerger.class)) {

            pdfUtilsMock.when(() -> PdfUtils.splitByPageLimit(testPdfBytes, 10))
                    .thenReturn(List.of(chunk));
            when(clovaOcrClient.recognizePdfBytes(chunk)).thenReturn(mockResponse1);
            mergerMock.when(() -> OcrResponseMerger.merge(List.of(mockResponse1)))
                    .thenReturn(expectedResponse);

            // when
            ClovaOcrResponse result = clovaOcrProvider.ocrPdfByUrl(TEST_PDF_URL);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            verify(pdfDownloadClient).downloadPdfFromUrl(TEST_PDF_URL);
            verify(clovaOcrClient).recognizePdfBytes(chunk);
        }
    }

    @Test
    @DisplayName("다중 청크 PDF OCR 처리 성공")
    void ocrPdfByUrl_Success_MultipleChunks() {
        // given
        byte[] chunk1 = "chunk1".getBytes();
        byte[] chunk2 = "chunk2".getBytes();
        ClovaOcrResponse mergedResponse = ClovaOcrResponse.createEmpty();

        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL)).thenReturn(testPdfBytes);

        try (MockedStatic<PdfUtils> pdfUtilsMock = mockStatic(PdfUtils.class);
             MockedStatic<OcrResponseMerger> mergerMock = mockStatic(OcrResponseMerger.class)) {

            pdfUtilsMock.when(() -> PdfUtils.splitByPageLimit(testPdfBytes, 10))
                    .thenReturn(List.of(chunk1, chunk2));
            when(clovaOcrClient.recognizePdfBytes(chunk1)).thenReturn(mockResponse1);
            when(clovaOcrClient.recognizePdfBytes(chunk2)).thenReturn(mockResponse2);
            mergerMock.when(() -> OcrResponseMerger.merge(List.of(mockResponse1, mockResponse2)))
                    .thenReturn(mergedResponse);

            // when
            ClovaOcrResponse result = clovaOcrProvider.ocrPdfByUrl(TEST_PDF_URL);

            // then
            assertThat(result).isEqualTo(mergedResponse);
            verify(pdfDownloadClient).downloadPdfFromUrl(TEST_PDF_URL);
            verify(clovaOcrClient).recognizePdfBytes(chunk1);
            verify(clovaOcrClient).recognizePdfBytes(chunk2);
        }
    }

    @Test
    @DisplayName("PDF 다운로드 실패 시 예외 전파")
    void ocrPdfByUrl_ThrowsException_WhenDownloadFails() {
        // given
        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL))
                .thenThrow(new OcrException(OcrErrorType.PDF_DOWNLOAD_ERROR));

        // when & then
        assertThatThrownBy(() -> clovaOcrProvider.ocrPdfByUrl(TEST_PDF_URL))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_DOWNLOAD_ERROR);

        verify(pdfDownloadClient).downloadPdfFromUrl(TEST_PDF_URL);
        verifyNoInteractions(clovaOcrClient);
    }

    @Test
    @DisplayName("PDF 분할 실패 시 예외 전파")
    void ocrPdfByUrl_ThrowsException_WhenSplitFails() {
        // given
        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL)).thenReturn(testPdfBytes);

        try (MockedStatic<PdfUtils> pdfUtilsMock = mockStatic(PdfUtils.class)) {
            pdfUtilsMock.when(() -> PdfUtils.splitByPageLimit(testPdfBytes, 10))
                    .thenThrow(new OcrException(OcrErrorType.PDF_SPLIT_ERROR));

            // when & then
            assertThatThrownBy(() -> clovaOcrProvider.ocrPdfByUrl(TEST_PDF_URL))
                    .isInstanceOf(OcrException.class)
                    .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_SPLIT_ERROR);

            verify(pdfDownloadClient).downloadPdfFromUrl(TEST_PDF_URL);
            verifyNoInteractions(clovaOcrClient);
        }
    }

    @Test
    @DisplayName("OCR 클라이언트 호출 실패 시 예외 전파")
    void ocrPdfByUrl_ThrowsException_WhenOcrFails() {
        // given
        byte[] chunk = "chunk1".getBytes();

        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL)).thenReturn(testPdfBytes);

        try (MockedStatic<PdfUtils> pdfUtilsMock = mockStatic(PdfUtils.class)) {
            pdfUtilsMock.when(() -> PdfUtils.splitByPageLimit(testPdfBytes, 10))
                    .thenReturn(List.of(chunk));
            when(clovaOcrClient.recognizePdfBytes(chunk))
                    .thenThrow(new OcrException(OcrErrorType.OCR_CLIENT_ERROR));

            // when & then
            assertThatThrownBy(() -> clovaOcrProvider.ocrPdfByUrl(TEST_PDF_URL))
                    .isInstanceOf(OcrException.class)
                    .hasFieldOrPropertyWithValue("errorType", OcrErrorType.OCR_CLIENT_ERROR);

            verify(clovaOcrClient).recognizePdfBytes(chunk);
        }
    }

    @Test
    @DisplayName("텍스트 추출까지 포함한 전체 플로우 성공")
    void ocrPdfTextByUrl_Success() {
        // given
        byte[] chunk = "chunk1".getBytes();
        ClovaOcrResponse ocrResponse = ClovaOcrResponse.createEmpty();
        String expectedText = "Extracted text content";

        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL)).thenReturn(testPdfBytes);

        try (MockedStatic<PdfUtils> pdfUtilsMock = mockStatic(PdfUtils.class);
             MockedStatic<OcrResponseMerger> mergerMock = mockStatic(OcrResponseMerger.class);
             MockedStatic<OcrTextExtractor> extractorMock = mockStatic(OcrTextExtractor.class)) {

            pdfUtilsMock.when(() -> PdfUtils.splitByPageLimit(testPdfBytes, 10))
                    .thenReturn(List.of(chunk));
            when(clovaOcrClient.recognizePdfBytes(chunk)).thenReturn(mockResponse1);
            mergerMock.when(() -> OcrResponseMerger.merge(List.of(mockResponse1)))
                    .thenReturn(ocrResponse);
            extractorMock.when(() -> OcrTextExtractor.toPlainText(ocrResponse))
                    .thenReturn(expectedText);

            // when
            String result = clovaOcrProvider.ocrPdfTextByUrl(TEST_PDF_URL);

            // then
            assertThat(result).isEqualTo(expectedText);
            verify(pdfDownloadClient).downloadPdfFromUrl(TEST_PDF_URL);
            verify(clovaOcrClient).recognizePdfBytes(chunk);
        }
    }

    @Test
    @DisplayName("텍스트 추출 중 OCR 실패 시 예외 전파")
    void ocrPdfTextByUrl_ThrowsException_WhenOcrFails() {
        // given
        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL))
                .thenThrow(new OcrException(OcrErrorType.PDF_DOWNLOAD_ERROR));

        // when & then
        assertThatThrownBy(() -> clovaOcrProvider.ocrPdfTextByUrl(TEST_PDF_URL))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_DOWNLOAD_ERROR);
    }

    @Test
    @DisplayName("빈 청크 리스트 처리")
    void ocrPdfByUrl_WithEmptyChunks() {
        // given
        ClovaOcrResponse emptyResponse = ClovaOcrResponse.createEmpty();

        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL)).thenReturn(testPdfBytes);

        try (MockedStatic<PdfUtils> pdfUtilsMock = mockStatic(PdfUtils.class);
             MockedStatic<OcrResponseMerger> mergerMock = mockStatic(OcrResponseMerger.class)) {

            pdfUtilsMock.when(() -> PdfUtils.splitByPageLimit(testPdfBytes, 10))
                    .thenReturn(List.of());
            mergerMock.when(() -> OcrResponseMerger.merge(List.of()))
                    .thenReturn(emptyResponse);

            // when
            ClovaOcrResponse result = clovaOcrProvider.ocrPdfByUrl(TEST_PDF_URL);

            // then
            assertThat(result).isEqualTo(emptyResponse);
            verify(pdfDownloadClient).downloadPdfFromUrl(TEST_PDF_URL);
            verifyNoInteractions(clovaOcrClient);
        }
    }

    @Test
    @DisplayName("10페이지 이상 PDF 처리 - 정확히 20페이지")
    void ocrPdfByUrl_ExactlyTwoChunks() {
        // given
        byte[] chunk1 = "chunk1".getBytes();
        byte[] chunk2 = "chunk2".getBytes();
        ClovaOcrResponse mergedResponse = ClovaOcrResponse.createEmpty();

        when(pdfDownloadClient.downloadPdfFromUrl(TEST_PDF_URL)).thenReturn(testPdfBytes);

        try (MockedStatic<PdfUtils> pdfUtilsMock = mockStatic(PdfUtils.class);
             MockedStatic<OcrResponseMerger> mergerMock = mockStatic(OcrResponseMerger.class)) {

            pdfUtilsMock.when(() -> PdfUtils.splitByPageLimit(testPdfBytes, 10))
                    .thenReturn(List.of(chunk1, chunk2));
            when(clovaOcrClient.recognizePdfBytes(chunk1)).thenReturn(mockResponse1);
            when(clovaOcrClient.recognizePdfBytes(chunk2)).thenReturn(mockResponse2);
            mergerMock.when(() -> OcrResponseMerger.merge(List.of(mockResponse1, mockResponse2)))
                    .thenReturn(mergedResponse);

            // when
            ClovaOcrResponse result = clovaOcrProvider.ocrPdfByUrl(TEST_PDF_URL);

            // then
            assertThat(result).isEqualTo(mergedResponse);
            verify(clovaOcrClient, times(2)).recognizePdfBytes(any());
        }
    }
}