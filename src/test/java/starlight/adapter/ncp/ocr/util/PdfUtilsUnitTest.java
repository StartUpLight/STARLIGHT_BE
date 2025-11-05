package starlight.adapter.ncp.ocr.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.ncp.ocr.exception.OcrErrorType;
import starlight.adapter.ncp.ocr.exception.OcrException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PdfUtils 테스트")
class PdfUtilsUnitTest {

    @Test
    @DisplayName("페이지 수가 maxPagesPerChunk 이하면 분할하지 않음")
    void splitByPageLimit_NoSplit_WhenPagesLessThanLimit() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(5);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo(pdfBytes);
    }

    @Test
    @DisplayName("정확히 maxPagesPerChunk 페이지면 분할하지 않음")
    void splitByPageLimit_NoSplit_WhenPagesExactlyLimit() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(10);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo(pdfBytes);
    }

    @Test
    @DisplayName("11페이지 PDF는 2개 청크로 분할")
    void splitByPageLimit_TwoChunks_When11Pages() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(11);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(2);
        assertThat(getPdfPageCount(chunks.get(0))).isEqualTo(10);
        assertThat(getPdfPageCount(chunks.get(1))).isEqualTo(1);
    }

    @Test
    @DisplayName("20페이지 PDF는 2개 청크로 분할")
    void splitByPageLimit_TwoChunks_When20Pages() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(20);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(2);
        assertThat(getPdfPageCount(chunks.get(0))).isEqualTo(10);
        assertThat(getPdfPageCount(chunks.get(1))).isEqualTo(10);
    }

    @Test
    @DisplayName("25페이지 PDF는 3개 청크로 분할")
    void splitByPageLimit_ThreeChunks_When25Pages() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(25);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(3);
        assertThat(getPdfPageCount(chunks.get(0))).isEqualTo(10);
        assertThat(getPdfPageCount(chunks.get(1))).isEqualTo(10);
        assertThat(getPdfPageCount(chunks.get(2))).isEqualTo(5);
    }

    @Test
    @DisplayName("1페이지 PDF 처리")
    void splitByPageLimit_SinglePage() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(1);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(1);
        assertThat(getPdfPageCount(chunks.get(0))).isEqualTo(1);
    }

    @Test
    @DisplayName("maxPagesPerChunk가 1일 때 각 페이지가 개별 청크로 분할")
    void splitByPageLimit_OnePagePerChunk() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(3);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 1);

        // then
        assertThat(chunks).hasSize(3);
        assertThat(getPdfPageCount(chunks.get(0))).isEqualTo(1);
        assertThat(getPdfPageCount(chunks.get(1))).isEqualTo(1);
        assertThat(getPdfPageCount(chunks.get(2))).isEqualTo(1);
    }

    @Test
    @DisplayName("잘못된 PDF 바이트 입력 시 예외 발생")
    void splitByPageLimit_ThrowsException_WhenInvalidPdfBytes() {
        // given
        byte[] invalidPdfBytes = "not a pdf".getBytes();

        // when & then
        assertThatThrownBy(() -> PdfUtils.splitByPageLimit(invalidPdfBytes, 10))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_SPLIT_ERROR);
    }

    @Test
    @DisplayName("빈 바이트 배열 입력 시 예외 발생")
    void splitByPageLimit_ThrowsException_WhenEmptyBytes() {
        // given
        byte[] emptyBytes = new byte[0];

        // when & then
        assertThatThrownBy(() -> PdfUtils.splitByPageLimit(emptyBytes, 10))
                .isInstanceOf(OcrException.class)
                .hasFieldOrPropertyWithValue("errorType", OcrErrorType.PDF_SPLIT_ERROR);
    }

    @Test
    @DisplayName("큰 PDF 분할 - 100페이지")
    void splitByPageLimit_LargePdf_100Pages() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(100);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(10);
        for (byte[] chunk : chunks) {
            assertThat(getPdfPageCount(chunk)).isEqualTo(10);
        }
    }

    @Test
    @DisplayName("분할된 청크들의 총 페이지 수는 원본과 동일")
    void splitByPageLimit_TotalPagesMatchOriginal() throws IOException {
        // given
        int originalPages = 33;
        byte[] pdfBytes = createTestPdf(originalPages);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        int totalPages = chunks.stream()
                .mapToInt(this::getPdfPageCount)
                .sum();
        assertThat(totalPages).isEqualTo(originalPages);
    }

    @Test
    @DisplayName("maxPagesPerChunk가 원본보다 훨씬 클 때")
    void splitByPageLimit_LimitMuchLargerThanPages() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(5);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 1000);

        // then
        assertThat(chunks).hasSize(1);
        assertThat(getPdfPageCount(chunks.get(0))).isEqualTo(5);
    }

    @Test
    @DisplayName("각 청크는 유효한 PDF 문서")
    void splitByPageLimit_EachChunkIsValidPdf() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(15);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        for (byte[] chunk : chunks) {
            try (PDDocument doc = PDDocument.load(chunk)) {
                assertThat(doc.getNumberOfPages()).isGreaterThan(0);
            }
        }
    }

    @Test
    @DisplayName("페이지 순서 유지 확인")
    void splitByPageLimit_MaintainsPageOrder() throws IOException {
        // given
        byte[] pdfBytes = createTestPdf(25);

        // when
        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, 10);

        // then
        assertThat(chunks).hasSize(3);
        assertThat(getPdfPageCount(chunks.get(0))).isEqualTo(10); // 1-10페이지
        assertThat(getPdfPageCount(chunks.get(1))).isEqualTo(10); // 11-20페이지
        assertThat(getPdfPageCount(chunks.get(2))).isEqualTo(5);  // 21-25페이지
    }

    // 헬퍼 메서드
    private byte[] createTestPdf(int pageCount) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pageCount; i++) {
                document.addPage(new PDPage());
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                document.save(out);
                return out.toByteArray();
            }
        }
    }

    private int getPdfPageCount(byte[] pdfBytes) {
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            return doc.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PDF", e);
        }
    }
}