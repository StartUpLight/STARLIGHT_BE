package starlight.application.infrastructure.provided;

import starlight.shared.dto.ClovaOcrResponse;
import starlight.adapter.ncp.ocr.exception.OcrException;

/**
 * OCR 제공자 추상화.
 * - 구현체 예: ClovaOcrProvider
 */
public interface OcrProvider {

    /**
     * 지정한 PDF URL을 전체 페이지 OCR 처리한 뒤, 단일 응답으로 병합해 반환.
     */
    ClovaOcrResponse ocrPdfByUrl(String pdfUrl) throws OcrException;

    /**
     * 지정한 PDF URL을 OCR 처리하여 평문 텍스트로 반환.
     */
    String ocrPdfTextByUrl(String pdfUrl, double minConfidence) throws OcrException;
}
