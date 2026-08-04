package starlight.application.aireport.required;

import starlight.application.aireport.provided.dto.AiReportResult;

public interface AiReportPdfRenderPort {

    byte[] render(AiReportResult report);
}
