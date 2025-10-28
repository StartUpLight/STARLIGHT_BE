package starlight.application.businessplan.provided;

import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.dto.SectionResponse;

import java.util.List;

public interface SectionCrudService {

    SectionResponse.Retrieved getSection(Long planId, SectionName sectionName);

    SectionResponse.Created createOrUpdateSection(Long planId, SectionRequest request);

    SectionResponse.Deleted deleteSection(Long planId, SectionName sectionName);

    List<Boolean> checkSection(SectionRequest request);
}