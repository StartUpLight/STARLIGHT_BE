package starlight.order.toss.adapter.webapi.dto.response;

import starlight.order.toss.domain.Orders;

public record OrderPrepareResponse(
        String orderCode,
        Long amount,
        String status
) {
    public static OrderPrepareResponse from(Orders order) {
        return new OrderPrepareResponse(
                order.getOrderCode(),
                order.getPrice(),
                order.getStatus().name()
        );
    }
}