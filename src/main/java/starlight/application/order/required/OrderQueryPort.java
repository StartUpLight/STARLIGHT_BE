package starlight.application.order.required;

import starlight.domain.order.order.Orders;

import java.util.List;
import java.util.Optional;

public interface OrderQueryPort {

    Optional<Orders> findByOrderCode(String orderCode);

    List<Orders> findAllWithPaymentsByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    Orders getByOrderCodeOrThrow(String orderCode);
}
