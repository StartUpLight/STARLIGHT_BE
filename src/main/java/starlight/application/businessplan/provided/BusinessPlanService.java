package starlight.application.businessplan.provided;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.application.businessplan.dto.SubSectionResponse;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.List;

public interface BusinessPlanService {

    BusinessPlan createBusinessPlan(Long memberId);

    void deleteBusinessPlan(Long planId, Long memberId);

    BusinessPlan updateBusinessPlanTitle(Long planId, Long memberId, String title);

    SubSectionResponse.Created createOrUpdateSection(Long planId, JsonNode jsonNode, SubSectionName subSectionName);

    SubSectionResponse.Retrieved getSubSection(Long planId, SubSectionName subSectionName);

    SubSectionResponse.Deleted deleteSubSection(Long planId, SubSectionName subSectionName);

    List<Boolean> checkSubSection(Long planId, JsonNode jsonNode, SubSectionName subSectionName);
}
