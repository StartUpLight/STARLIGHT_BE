package starlight.application.order.provided;

import starlight.domain.order.order.Orders;

import java.util.List;
import java.util.Optional;

public interface OrdersQuery {

    Optional<Orders> findByOrderCode(String orderCode);

    List<Orders> findAllWithPaymentsByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    Orders getByOrderCodeOrThrow(String orderCode);

    Orders save(Orders order);
}