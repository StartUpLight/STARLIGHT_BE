package starlight.order.toss.adapter.order.webapi.dto.response;

import starlight.order.toss.adapter.order.webapi.dto.TossClientResponse;

public record OrderCancelResponse(
        String orderId,
        String paymentKey,
        String status,
        Integer totalAmount
) {
    public static OrderCancelResponse from(TossClientResponse.Cancel tossResponse) {
        return new OrderCancelResponse(
                tossResponse.orderId(),
                tossResponse.paymentKey(),
                tossResponse.status(),
                tossResponse.totalAmount()
        );
    }
}