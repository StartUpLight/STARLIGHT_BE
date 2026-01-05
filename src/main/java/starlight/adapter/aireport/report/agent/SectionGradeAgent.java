package starlight.adapter.aireport.report.agent;

import starlight.adapter.aireport.report.dto.SectionGradingResult;
import starlight.shared.enumerate.SectionType;

public interface SectionGradeAgent {

    SectionType getSectionType();

    SectionGradingResult gradeSection(String sectionContent);
    
    /**
     * SectionType의 tag를 기반으로 filter expression 생성
     */
    default String buildFilterExpression() {
        SectionType sectionType = getSectionType();
        String tag = sectionType.getTag();
        
        if (tag == null || tag.isBlank()) {
            return null;
        }

        return "tag == '" + tag + "'";
    }
}



