package starlight.application.aireport.provided;

import starlight.application.aireport.dto.AiReportResponse;

public interface AiReportService {
    AiReportResponse gradeBusinessPlan(Long businessPlanId, Long memberId);

    AiReportResponse getAiReport(Long businessPlanId, Long memberId);
}

