package starlight.adapter.order.webapi.dto.response;

import starlight.domain.order.order.Orders;

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