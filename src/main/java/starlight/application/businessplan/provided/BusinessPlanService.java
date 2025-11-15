package starlight.application.businessplan.provided;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.application.businessplan.provided.dto.BusinessPlanResponse;
import starlight.application.businessplan.provided.dto.SubSectionResponse;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface BusinessPlanService {

    BusinessPlanResponse.Result createBusinessPlan(Long memberId);

    BusinessPlanResponse.Result createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId);

    BusinessPlanResponse.Result getBusinessPlanInfo(Long planId, Long memberId);

    BusinessPlanResponse.Detail getBusinessPlanDetail(Long planId, Long memberId);

    List<BusinessPlanResponse.Preview> getBusinessPlanList(Long memberId);

    String updateBusinessPlanTitle(Long planId, String title, Long memberId);

    BusinessPlanResponse.Result deleteBusinessPlan(Long planId, Long memberId);

    SubSectionResponse.Result createOrUpdateSubSection(Long planId, JsonNode jsonNode, List<Boolean> checks,
            SubSectionType subSectionType, Long memberId);

    SubSectionResponse.Detail getSubSectionDetail(Long planId, SubSectionType subSectionType, Long memberId);

    List<Boolean> checkAndUpdateSubSection(Long planId, JsonNode jsonNode, SubSectionType subSectionType,
            Long memberId);

    SubSectionResponse.Result deleteSubSection(Long planId, SubSectionType subSectionType, Long memberId);
}
