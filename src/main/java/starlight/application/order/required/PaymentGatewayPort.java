package starlight.application.order.required;

import starlight.application.order.provided.dto.TossClientResult;

public interface PaymentGatewayPort {

    TossClientResult.Confirm confirm(String orderCode, String paymentKey, Long price);

    TossClientResult.Cancel cancel(String paymentKey, String reason);
}
