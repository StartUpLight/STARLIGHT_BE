package starlight.application.bussinessplan.provided;

import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.dto.SectionResponse;

public interface BusinessPlanService {

    SectionResponse.Created createSection(Long planId, SectionRequest request);

    SectionResponse.Retrieved getSection(Long planId, SectionName sectionName);

    SectionResponse.Updated updateSection(Long planId, SectionRequest request);

    SectionResponse.Deleted deleteSection(Long planId, SectionName sectionName);
}