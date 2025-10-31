package starlight.application.businessplan.required;

import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.List;

public interface ChecklistGrader {

    List<Boolean> check(SubSectionName subSectionName, String content);
}
