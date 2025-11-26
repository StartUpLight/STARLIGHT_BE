package starlight.order.toss.application.order.provided;

import starlight.order.toss.domain.order.Orders;

import java.util.Optional;

public interface OrdersQuery {

    Optional<Orders> findByOrderCode(String orderCode);

    Orders getByOrderCodeOrThrow(String orderCode);

    Orders save(Orders order);
}
