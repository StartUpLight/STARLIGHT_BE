package starlight.application.businessplan.required;

import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface ChecklistGrader {

    /**
     * 서브섹션 내용을 체크리스트 기준에 따라 체크합니다.
     * 
     * @param subSectionType 서브섹션 타입
     * @param newContent 새로운 서브섹션 내용
     * @param previousContent 이전 서브섹션 내용 (없으면 null)
     * @param previousChecks 이전 체크리스트 결과 (없으면 null)
     * @return 체크리스트 결과
     */
    List<Boolean> check(
            SubSectionType subSectionType,
            String newContent,
            String previousContent,
            List<Boolean> previousChecks
    );
}
