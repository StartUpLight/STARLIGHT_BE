package starlight.application.backoffice.order.provided;

import org.springframework.data.domain.Pageable;
import starlight.application.backoffice.order.provided.dto.result.BackofficePaymentPageResult;
import starlight.domain.order.enumerate.OrderStatus;

public interface BackofficePaymentQueryUseCase {

    BackofficePaymentPageResult findPayments(OrderStatus status, String keyword, Pageable pageable);
}
