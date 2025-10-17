package starlight.application.infrastructure.provided;

import starlight.shared.dto.ClovaOcrResponse;
import starlight.adapter.ncp.ocr.exception.OcrException;

public interface OcrProvider {

    ClovaOcrResponse ocrPdfByUrl(String pdfUrl) throws OcrException;

    String ocrPdfTextByUrl(String pdfUrl) throws OcrException;
}
