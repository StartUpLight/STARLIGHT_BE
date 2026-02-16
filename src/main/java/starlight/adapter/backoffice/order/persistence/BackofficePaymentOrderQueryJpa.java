package starlight.adapter.backoffice.order.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.order.required.BackofficePaymentOrderQueryPort;
import starlight.application.backoffice.order.required.dto.BackofficePaymentOrderLookupResult;
import starlight.domain.order.enumerate.OrderStatus;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BackofficePaymentOrderQueryJpa implements BackofficePaymentOrderQueryPort {

    private final BackofficeOrderRepository backofficeOrderRepository;

    @Override
    public Page<BackofficePaymentOrderLookupResult> findPaymentPage(
            OrderStatus status,
            String keyword,
            List<Long> memberIds,
            Pageable pageable
    ) {
        return backofficeOrderRepository.findPaymentPage(status, keyword, memberIds, pageable);
    }
}
