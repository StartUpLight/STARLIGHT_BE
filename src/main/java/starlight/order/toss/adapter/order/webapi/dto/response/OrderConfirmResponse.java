package starlight.order.toss.adapter.order.webapi.dto.response;

import starlight.order.toss.domain.order.Orders;
import starlight.order.toss.domain.order.PaymentRecords;

import java.time.Instant;

public record OrderConfirmResponse(
        Long buyerId,
        String paymentKey,
        String orderId,
        Long amount,
        String status,
        Instant approvedAt,
        String receiptUrl,
        String method,
        String provider
) {
    public static OrderConfirmResponse from(Orders order) {
        PaymentRecords payment = order.getLatestPaymentOrThrow();

        return new OrderConfirmResponse(
                order.getBuyerId(),
                payment.getPaymentKey(),
                order.getOrderCode(),
                order.getPrice(),
                order.getStatus().name(),
                payment.getApprovedAt(),
                payment.getReceiptUrl(),
                payment.getMethod(),
                payment.getProvider()
        );
    }
}