package starlight.application.businessplan.provided;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Pageable;
import starlight.application.businessplan.provided.dto.BusinessPlanResult;
import starlight.application.businessplan.provided.dto.SubSectionResult;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface BusinessPlanUseCase {

    BusinessPlanResult.Result createBusinessPlan(Long memberId);

    BusinessPlanResult.Result createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId);

    BusinessPlanResult.Result getBusinessPlanInfo(Long planId, Long memberId);

    BusinessPlanResult.Detail getBusinessPlanDetail(Long planId, Long memberId);

    BusinessPlanResult.PreviewPage getBusinessPlanList(Long memberId, Pageable pageable);

    String updateBusinessPlanTitle(Long planId, String title, Long memberId);

    BusinessPlanResult.Result deleteBusinessPlan(Long planId, Long memberId);

    SubSectionResult.Result upsertSubSection(Long planId, JsonNode jsonNode, List<Boolean> checks,
                                             SubSectionType subSectionType, Long memberId);

    SubSectionResult.Detail getSubSectionDetail(Long planId, SubSectionType subSectionType, Long memberId);

    List<Boolean> checkAndUpdateSubSection(Long planId, JsonNode jsonNode, SubSectionType subSectionType,
            Long memberId);

    SubSectionResult.Result deleteSubSection(Long planId, SubSectionType subSectionType, Long memberId);


}
