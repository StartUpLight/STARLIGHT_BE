package starlight.adapter.order.webapi.dto.response;

import starlight.application.order.provided.dto.TossClientResult;

public record OrderCancelResponse(
        String orderId,
        String paymentKey,
        String status,
        Integer totalAmount
) {
    public static OrderCancelResponse from(TossClientResult.Cancel tossResponse) {
        return new OrderCancelResponse(
                tossResponse.orderId(),
                tossResponse.paymentKey(),
                tossResponse.status(),
                tossResponse.totalAmount()
        );
    }
}