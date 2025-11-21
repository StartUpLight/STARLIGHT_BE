package starlight.application.aireport.provided;

import starlight.application.aireport.provided.dto.AiReportResponse;

public interface AiReportService {
    AiReportResponse gradeBusinessPlan(Long businessPlanId, Long memberId);

    AiReportResponse createAndGradePdfBusinessPlan(String title, String pdfUrl, Long memberId);

    AiReportResponse getAiReport(Long businessPlanId, Long memberId);
}