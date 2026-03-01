package starlight.application.backoffice.member.provided;

import org.springframework.data.domain.Pageable;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserBusinessPlanPageResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserDashboardResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPageResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPaymentResult;

public interface BackofficeUserQueryUseCase {

    BackofficeUserDashboardResult getDashboard();

    BackofficeUserPageResult findUsers(String keyword, Pageable pageable);

    BackofficeUserBusinessPlanPageResult findUserBusinessPlans(Long userId, String scoreFilter, Pageable pageable);

    BackofficeUserPaymentResult findUserPayments(Long userId, Pageable pageable);
}
