package starlight.order.toss.application.provided;

import starlight.order.toss.domain.Orders;

import java.util.Optional;

public interface OrdersQuery {

    Optional<Orders> findByOrderCode(String orderCode);

    Orders getByOrderCodeOrThrow(String orderCode);

    boolean existsPaidByBuyerIdAndBusinessPlanId(Long buyerId, Long businessPlanId);

    Orders save(Orders order);
}
