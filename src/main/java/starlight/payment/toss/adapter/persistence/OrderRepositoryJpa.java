package starlight.payment.toss.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.payment.toss.application.provided.OrdersQuery;
import starlight.payment.toss.domain.Orders;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryJpa implements OrdersQuery {

    private final OrdersRepository repository;

    @Override
    public Orders findByOrderCode(String orderCode) {
        return repository.findByOrderCode(orderCode).orElseThrow(
                () -> new IllegalStateException("주문이 없습니다: " + orderCode)
        );
    }
}
