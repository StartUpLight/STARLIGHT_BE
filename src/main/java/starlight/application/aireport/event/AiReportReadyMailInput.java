package starlight.application.aireport.event;

/**
 * AI 리포트(PDF) 완료 안내 메일 발송에 필요한 입력.
 * {@code filename}은 첨부 파일명과 메일 제목 따옴표 안 표시에 동일하게 사용한다.
 */
public record AiReportReadyMailInput(
        String toEmail,
        String recipientName,
        String reportUrl,
        String filename,
        byte[] pdfBytes
) {
    public static AiReportReadyMailInput of(
            String toEmail,
            String recipientName,
            String reportUrl,
            String filename,
            byte[] pdfBytes
    ) {
        return new AiReportReadyMailInput(toEmail, recipientName, reportUrl, filename, pdfBytes);
    }
}
