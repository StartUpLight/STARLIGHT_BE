package starlight.order.toss.adapter.webapi.dto.response;

import starlight.order.toss.domain.Orders;
import starlight.order.toss.domain.PaymentRecords;

import java.time.Instant;

/**
 * 결제 승인 응답
 */
public record OrderConfirmResponse(
        Long businessPlanId,
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
                order.getBusinessPlanId(),
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