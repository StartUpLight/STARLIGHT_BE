package starlight.order.toss.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.order.toss.domain.Orders;
import starlight.order.toss.domain.enumerate.OrderStatus;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByOrderCode(String orderCode);

    boolean existsByBuyerIdAndBusinessPlanIdAndStatus(Long buyerId, Long businessPlanId, OrderStatus status);
}
