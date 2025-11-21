package starlight.application.businessplan.required;

import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface ChecklistGrader {

    /**
     * 서브섹션 내용을 체크리스트 기준에 따라 체크합니다.
     * 
     * @param subSectionType 서브섹션 타입
     * @param content 서브섹션 내용
     * @return 체크리스트 결과
     */
    List<Boolean> check(
            SubSectionType subSectionType,
            String content
    );
}
