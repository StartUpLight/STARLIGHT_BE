package starlight.application.businessplan.required;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanQuery {

    BusinessPlan getOrThrow(Long id);

    BusinessPlan getOrThrowWithAllSubSections(Long id);

    BusinessPlan save(BusinessPlan businessPlan);

    void delete(BusinessPlan businessPlan);

    Page<BusinessPlan> findPreviewPage(Long memberId, Pageable pageable);
}
