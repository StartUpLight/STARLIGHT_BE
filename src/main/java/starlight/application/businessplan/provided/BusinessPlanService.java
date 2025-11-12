package starlight.application.businessplan.provided;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.application.businessplan.dto.SubSectionResponse;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface BusinessPlanService {

    BusinessPlan createBusinessPlan(Long memberId);

    void deleteBusinessPlan(Long planId, Long memberId);

    BusinessPlan updateBusinessPlanTitle(Long planId, Long memberId, String title);

    SubSectionResponse.Created createOrUpdateSubSection(Long planId, JsonNode jsonNode, List<Boolean> checks, SubSectionType subSectionType, Long memberId);

    SubSectionResponse.Retrieved getSubSection(Long planId, SubSectionType subSectionType, Long memberId);

    SubSectionResponse.Deleted deleteSubSection(Long planId, SubSectionType subSectionType, Long memberId);

    List<Boolean> checkAndUpdateSubSection(Long planId, JsonNode jsonNode, SubSectionType subSectionType, Long memberId);

    List<BusinessPlan> getBusinessPlanList(Long memberId);

    List<SubSectionResponse.Snapshot> getBusinessPlanSubSections(Long planId, Long memberId);
}
