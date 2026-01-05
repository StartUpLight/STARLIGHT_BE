package starlight.application.businessplan.required;

import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface ChecklistGraderPort {

    List<Boolean> check(SubSectionType subSectionType, String content);
}
