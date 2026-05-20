package starlight.application.aireport.required;

import starlight.application.aireport.provided.dto.pdf.AiReportPdfView;

public interface AiReportPdfRenderPort {

    byte[] render(AiReportPdfView view);
}
