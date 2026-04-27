package starlight.application.aireport.event;

public record PdfReportRequestedEvent(long businessPlanId, String pdfUrl, long memberId) {
}
