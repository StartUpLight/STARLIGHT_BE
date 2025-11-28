package starlight.adapter.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.order.order.Orders;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByOrderCode(String orderCode);

    @Query("""
    select distinct o
    from Orders o
    left join fetch o.payments p
    where o.buyerId = :buyerId
    order by o.createdAt desc
    """)
    List<Orders> findAllWithPaymentsByBuyerIdOrderByCreatedAtDesc(@Param("buyerId") Long buyerId);

}
