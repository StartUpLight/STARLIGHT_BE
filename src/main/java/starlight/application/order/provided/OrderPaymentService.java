package starlight.application.order.provided;

import starlight.application.order.provided.dto.TossClientResponse;
import starlight.adapter.order.webapi.dto.request.OrderCancelRequest;
import starlight.application.order.provided.dto.PaymentHistoryItemDto;
import starlight.domain.order.order.Orders;

import java.util.List;

public interface OrderPaymentService{

    Orders prepare(String orderCodeStr, Long buyerId, String productCode);

    Orders confirm(String orderCodeStr, String paymentKey, Long buyerId);

    TossClientResponse.Cancel cancel(OrderCancelRequest request);

    List<PaymentHistoryItemDto> getPaymentHistory(Long buyerId);
}
