package starlight.application.backoffice.order.required;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import starlight.application.backoffice.order.required.dto.BackofficePaymentOrderLookupResult;
import starlight.domain.order.enumerate.OrderStatus;

import java.util.List;

public interface BackofficePaymentOrderQueryPort {

    Page<BackofficePaymentOrderLookupResult> findPaymentPage(
            OrderStatus status,
            String keyword,
            List<Long> memberIds,
            Pageable pageable
    );
}
