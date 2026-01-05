package starlight.application.aireport.required;

import starlight.application.aireport.provided.dto.AiReportResult;

public interface AiReportGraderPort {
    AiReportResult gradeContentWithSectionAgents(String content);
    private AiReportResult gradeContentWithSections(String content) { }
    private AiReportResult gradeContent(String content) { }
}

