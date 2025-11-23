package starlight.order.toss.adapter.order.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.order.toss.application.order.provided.OrdersQuery;
import starlight.order.toss.domain.order.Orders;
import starlight.order.toss.domain.enumerate.OrderStatus;
import starlight.order.toss.domain.exception.OrderErrorType;
import starlight.order.toss.domain.exception.OrderException;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryJpa implements OrdersQuery {

    private final OrdersRepository repository;

    @Override
    public Optional<Orders> findByOrderCode(String orderCode) {
        return repository.findByOrderCode(orderCode);
    }

    @Override
    public Orders getByOrderCodeOrThrow(String orderCode) {
        return repository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_NOT_FOUND));
    }

    @Override
    public boolean existsPaidByBuyerIdAndBusinessPlanId(Long buyerId, Long businessPlanId) {
        return repository.existsByBuyerIdAndBusinessPlanIdAndStatus(
                buyerId, businessPlanId, OrderStatus.PAID
        );
    }

    @Override
    public Orders save(Orders order) {
        return repository.save(order);
    }
}
