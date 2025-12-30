package starlight.adapter.aireport.infrastructure.ocr.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrErrorType;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class PdfUtils {

        private PdfUtils() {}

    /**
     * PDF를 페이지 단위로 잘라 maxPagesPerChunk 크기의 조각들로 분할하여 반환합니다.
     *
     * @param sourcePdfBytes     원본 PDF 바이트
     * @param maxPagesPerChunk   조각당 최대 페이지 수(예: 10)
     * @return 분할된 PDF 바이트 배열 목록(페이지 순서 유지)
     */
    public static List<byte[]> splitByPageLimit(byte[] sourcePdfBytes, int maxPagesPerChunk) {
        try (PDDocument sourceDoc = PDDocument.load(new ByteArrayInputStream(sourcePdfBytes))) {

            int totalPages = sourceDoc.getNumberOfPages();
            if (totalPages <= maxPagesPerChunk) {
                return List.of(sourcePdfBytes);
            }

            List<byte[]> chunks = new ArrayList<>();
            int startPageIndex = 0;

            while (startPageIndex < totalPages) {
                int endPageIndexExclusive = Math.min(startPageIndex + maxPagesPerChunk, totalPages);

                // 부분 문서 생성
                try (PDDocument chunkDoc = new PDDocument()) {
                    for (int pageIndex = startPageIndex; pageIndex < endPageIndexExclusive; pageIndex++) {
                        chunkDoc.addPage(sourceDoc.getPage(pageIndex));
                    }
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        chunkDoc.save(out);
                        chunks.add(out.toByteArray());
                    }
                }

                startPageIndex = endPageIndexExclusive;
            }

            return chunks;
        } catch (Exception e) {
            log.error("PDF 분할 실패: {}", e.getMessage());
            throw new OcrException(OcrErrorType.PDF_SPLIT_ERROR);
        }
    }
}
