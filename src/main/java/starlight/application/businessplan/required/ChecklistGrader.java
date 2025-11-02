package starlight.application.businessplan.required;

import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface ChecklistGrader {

    List<Boolean> check(SubSectionType subSectionType, String content);
}
