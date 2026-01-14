package starlight.adapter.aireport.report.agent;

import starlight.application.aireport.provided.dto.AiReportResult;

public interface FullReportGradeAgent {

    AiReportResult gradeFullReport(String content);
}
