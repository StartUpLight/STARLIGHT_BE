package starlight.payment.toss.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.payment.toss.domain.Orders;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByOrderCode(String orderCode);
}
