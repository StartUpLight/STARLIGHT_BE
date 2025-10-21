package starlight.application.infrastructure.provided;

import starlight.shared.dto.ClovaOcrResponse;

public interface OcrProvider {

    ClovaOcrResponse ocrPdfByUrl(String pdfUrl) ;

    String ocrPdfTextByUrl(String pdfUrl);
}
