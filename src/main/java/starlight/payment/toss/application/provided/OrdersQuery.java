package starlight.payment.toss.application.provided;

import starlight.payment.toss.domain.Orders;

public interface OrdersQuery {

    Orders findByOrderCode(String orderCode);
}
