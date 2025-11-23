package starlight.order.toss.adapter.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.order.toss.domain.order.Orders;
import starlight.order.toss.domain.enumerate.OrderStatus;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByOrderCode(String orderCode);
}
