package starlight.application.order.required;

import starlight.domain.order.order.Orders;

public interface OrderCommandPort {

    Orders save(Orders order);
}
