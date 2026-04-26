package starlight.application.aireport.provided;

import starlight.application.aireport.provided.dto.AiReportResult;

public interface AiReportUseCase {
    AiReportResult gradeBusinessPlan(Long businessPlanId, Long memberId);

    void requestCreateAndGradePdfBusinessPlan(String title, String pdfUrl, Long memberId);

    AiReportResult getAiReport(Long businessPlanId, Long memberId);
}