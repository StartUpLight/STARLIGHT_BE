package starlight.adapter.order.webapi.dto.response;

import starlight.domain.order.order.Orders;
import starlight.domain.order.order.PaymentRecords;

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
        PaymentRecords done = order.getLatestPaymentOrThrow();

        return new OrderConfirmResponse(
                order.getBuyerId(),
                done.getPaymentKey(),
                order.getOrderCode(),
                order.getPrice(),
                order.getStatus().name(),
                done.getApprovedAt(),
                done.getReceiptUrl(),
                done.getMethod(),
                done.getProvider()
        );
    }
}