package starlight.order.toss.adapter.order.webapi.dto.response;

import starlight.order.toss.domain.order.Orders;

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