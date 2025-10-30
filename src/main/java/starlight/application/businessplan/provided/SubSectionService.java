package starlight.application.businessplan.provided;

import starlight.adapter.businessplan.webapi.dto.SubSectionRequest;
import starlight.domain.businessplan.enumerate.SubSectionName;
import starlight.adapter.businessplan.webapi.dto.SubSectionResponse;

import java.util.List;

public interface SubSectionService {

    SubSectionResponse.Created createOrUpdateSection(Long planId, SubSectionRequest request);

    SubSectionResponse.Retrieved getSubSection(Long planId, SubSectionName subSectionName);

    SubSectionResponse.Deleted deleteSubSection(Long planId, SubSectionName subSectionName);

    List<Boolean> checkSubSection(Long planId, SubSectionRequest request);
}
