package starlight.adapter.order.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.order.provided.OrdersQuery;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.order.exception.OrderErrorType;
import starlight.domain.order.exception.OrderException;
import starlight.domain.order.order.Orders;

import java.util.List;
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
    public List<Orders> findAllWithPaymentsByBuyerIdOrderByCreatedAtDesc(Long buyerId) {
        return repository.findAllWithPaymentsByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    @Override
    public Orders getByOrderCodeOrThrow(String orderCode) {
        return repository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_NOT_FOUND));
    }

    @Override
    public Orders save(Orders order) {
        return repository.save(order);
    }
}
