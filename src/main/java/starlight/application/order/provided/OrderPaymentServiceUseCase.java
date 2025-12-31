package starlight.application.order.provided;

import starlight.application.order.provided.dto.PaymentHistoryItemResult;
import starlight.application.order.provided.dto.TossClientResult;
import starlight.domain.order.order.Orders;

import java.util.List;

public interface OrderPaymentServiceUseCase {

    Orders prepare(String orderCodeStr, Long buyerId, String productCode);

    Orders confirm(String orderCodeStr, String paymentKey, Long buyerId);

    TossClientResult.Cancel cancel(String orderCode, String reason);

    List<PaymentHistoryItemResult> getPaymentHistory(Long buyerId);
}
