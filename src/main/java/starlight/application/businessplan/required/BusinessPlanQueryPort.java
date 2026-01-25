package starlight.application.businessplan.required;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanQueryPort {

    BusinessPlan findByIdOrThrow(Long id);

    BusinessPlan findByIdWithAllSubSectionsOrThrow(Long id);

    Page<BusinessPlan> findPreviewPage(Long memberId, Pageable pageable);
}
