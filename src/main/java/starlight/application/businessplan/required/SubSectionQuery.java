package starlight.application.businessplan.required;

import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.Optional;

public interface SubSectionQuery {

    Optional<SubSection> findByBusinessPlanIdAndSubSectionName(Long businessPlanId, SubSectionName subSectionName);

    SubSection save(SubSection subSection);

    void delete(SubSection subSection);
}

