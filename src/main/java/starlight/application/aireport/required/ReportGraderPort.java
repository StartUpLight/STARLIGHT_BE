package starlight.application.aireport.required;

import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.shared.enumerate.SectionType;

import java.util.Map;

public interface ReportGraderPort {

    AiReportResult gradeWithSectionAgents(Map<SectionType, String> sectionContents, String fullContent);

    AiReportResult gradeWithFullPrompt(String content);
}

