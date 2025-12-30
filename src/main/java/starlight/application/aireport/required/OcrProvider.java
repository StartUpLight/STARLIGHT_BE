package starlight.application.aireport.required;

import starlight.shared.dto.infrastructure.OcrResponse;

public interface OcrProvider {

    OcrResponse ocrPdfByUrl(String pdfUrl) ;

    String ocrPdfTextByUrl(String pdfUrl);
}
