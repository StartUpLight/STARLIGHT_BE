package starlight.adapter.aireport.reportgrader.agent;

import starlight.adapter.aireport.reportgrader.dto.SectionGradingResult;
import starlight.shared.enumerate.SectionType;

public interface SectionGradeAgent {
    SectionType getSectionType();
    SectionGradingResult gradeSection(String sectionContent);
    
    /**
     * SectionType의 tag를 기반으로 filter expression 생성
     * SubSectionType의 tag는 사용하지 않음
     */
    default String buildFilterExpression() {
        SectionType sectionType = getSectionType();
        String tag = sectionType.getTag();
        
        if (tag == null || tag.isBlank()) {
            return null;
        }
        
        // 단순히 "tag == 'problem_recognition'" 형식으로 반환
        return "tag == '" + tag + "'";
    }
}



