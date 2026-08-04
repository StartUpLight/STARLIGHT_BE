package starlight.application.aireport.event;

public record PdfReportRequestedInput(long businessPlanId, String pdfUrl, long memberId) {
}
