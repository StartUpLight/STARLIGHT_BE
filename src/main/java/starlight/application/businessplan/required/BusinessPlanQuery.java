package starlight.application.businessplan.required;

import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.Optional;

public interface BusinessPlanQuery {

    BusinessPlan getOrThrow(Long id);

    BusinessPlan save(BusinessPlan businessPlan);

    void delete(BusinessPlan businessPlan);

    Optional<SubSection> findSubSectionByParentSectionIdAndName(Long parentSectionId, SubSectionName subSectionName);

    SubSection saveSubSection(SubSection subSection);

    void deleteSubSection(SubSection subSection);

    void deleteSubSectionsByParentSectionId(Long parentSectionId);
}
