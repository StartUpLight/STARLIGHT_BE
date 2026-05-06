package starlight.application.order;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.application.order.provided.dto.TossClientResult;
import starlight.application.order.required.OrderCommandPort;
import starlight.application.order.required.OrderNotificationPort;
import starlight.application.order.required.OrderQueryPort;
import starlight.application.order.required.PaymentGatewayPort;
import starlight.application.order.required.UsageCreditChargePort;
import starlight.domain.order.enumerate.UsageProductType;
import starlight.domain.order.order.Orders;
import starlight.domain.order.order.vo.Money;
import starlight.domain.order.order.vo.OrderCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderPaymentServiceUnitTest {

    private final PaymentGatewayPort paymentGatewayPort = mock(PaymentGatewayPort.class);
    private final OrderQueryPort orderQueryPort = mock(OrderQueryPort.class);
    private final OrderCommandPort orderCommandPort = mock(OrderCommandPort.class);
    private final UsageCreditChargePort usageCreditChargePort = mock(UsageCreditChargePort.class);
    private final OrderNotificationPort orderNotificationPort = mock(OrderNotificationPort.class);

    private final OrderPaymentService sut = new OrderPaymentService(
            paymentGatewayPort,
            orderQueryPort,
            orderCommandPort,
            usageCreditChargePort,
            orderNotificationPort
    );

    @Test
    void 결제승인에_성공하면_구매자에게_알림을_보낸다() {
        UsageProductType product = UsageProductType.AI_REPORT_1;
        Orders order = Orders.newUsageOrder(
                OrderCode.of("order-1"),
                1L,
                Money.krw(product.getPrice()),
                product
        );
        ReflectionTestUtils.setField(order, "id", 20L);
        order.addPaymentAttempt(Money.krw(product.getPrice()));

        TossClientResult.Confirm confirmResponse = new TossClientResult.Confirm(
                "payment-key",
                "order-1",
                "DONE",
                "CARD",
                product.getPrice(),
                OffsetDateTime.now(),
                null,
                new TossClientResult.Confirm.Receipt("https://receipt.example.com")
        );

        when(orderQueryPort.getByOrderCodeOrThrow("order-1")).thenReturn(order);
        when(paymentGatewayPort.confirm("order-1", "payment-key", product.getPrice())).thenReturn(confirmResponse);
        when(orderCommandPort.save(order)).thenReturn(order);

        Orders savedOrder = sut.confirm("order-1", "payment-key", 1L);

        assertThat(savedOrder).isSameAs(order);
        verify(usageCreditChargePort).chargeForOrder(1L, 20L, product.getUsageCount());
        verify(orderNotificationPort).sendPaymentCompleted(
                1L,
                20L,
                product.getDescription(),
                product.getUsageCount()
        );
    }

    @Test
    void 결제준비는_알림을_보내지_않는다() {
        UsageProductType product = UsageProductType.AI_REPORT_1;
        Orders order = Orders.newUsageOrder(
                OrderCode.of("order-1"),
                1L,
                Money.krw(product.getPrice()),
                product
        );

        when(orderQueryPort.findByOrderCode("order-1")).thenReturn(Optional.empty());
        when(orderCommandPort.save(org.mockito.ArgumentMatchers.any(Orders.class))).thenReturn(order);

        sut.prepare("order-1", 1L, product.getCode());

        verify(orderNotificationPort, org.mockito.Mockito.never()).sendPaymentCompleted(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt()
        );
    }
}
