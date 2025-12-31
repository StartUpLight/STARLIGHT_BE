package starlight.application.order.provided;

import starlight.application.order.provided.dto.PaymentHistoryItemDto;
import starlight.application.order.provided.dto.TossClientResponse;
import starlight.domain.order.order.Orders;

import java.util.List;

public interface OrderPaymentServiceUseCase {

    Orders prepare(String orderCodeStr, Long buyerId, String productCode);

    Orders confirm(String orderCodeStr, String paymentKey, Long buyerId);

    TossClientResponse.Cancel cancel(String orderCode, String reason);

    List<PaymentHistoryItemDto> getPaymentHistory(Long buyerId);
}
