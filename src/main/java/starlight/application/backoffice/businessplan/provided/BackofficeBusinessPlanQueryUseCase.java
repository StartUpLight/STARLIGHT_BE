package starlight.application.backoffice.businessplan.provided;

import org.springframework.data.domain.Pageable;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDashboardResult;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDetailResult;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanPageResult;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDate;

public interface BackofficeBusinessPlanQueryUseCase {

    BackofficeBusinessPlanDetailResult findBusinessPlanDetail(Long planId);

    BackofficeBusinessPlanPageResult findBusinessPlans(PlanStatus status, String keyword, Pageable pageable);

    BackofficeBusinessPlanDashboardResult getDashboard(
            PlanStatus status,
            String keyword,
            LocalDate dateFrom,
            LocalDate dateTo
    );
}
