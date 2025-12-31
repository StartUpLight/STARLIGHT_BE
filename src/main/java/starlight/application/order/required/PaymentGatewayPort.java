package starlight.application.order.required;

import starlight.application.order.provided.dto.TossClientResponse;

public interface PaymentGatewayPort {

    TossClientResponse.Confirm confirm(String orderCode, String paymentKey, Long price);

    TossClientResponse.Cancel cancel(String paymentKey, String reason);
}
