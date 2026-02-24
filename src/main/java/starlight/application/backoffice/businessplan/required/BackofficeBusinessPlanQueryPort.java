package starlight.application.backoffice.businessplan.required;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BackofficeBusinessPlanQueryPort {

    BusinessPlan findByIdWithAllSubSectionsOrThrow(Long planId);

    Page<BusinessPlan> findBusinessPlanPage(
            PlanStatus status,
            String keyword,
            List<Long> memberIds,
            Pageable pageable
    );

    List<BusinessPlan> findBusinessPlansForDashboard(
            PlanStatus status,
            String keyword,
            List<Long> memberIds,
            LocalDateTime from,
            LocalDateTime to
    );
}
