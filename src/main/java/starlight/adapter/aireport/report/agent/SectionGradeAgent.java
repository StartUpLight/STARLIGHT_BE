package starlight.adapter.aireport.report.agent;

import starlight.adapter.aireport.report.dto.SectionGradingResult;
import starlight.shared.enumerate.SectionType;

public interface SectionGradeAgent {

    SectionType getSectionType();

    SectionGradingResult gradeSection(String sectionContent);
}



