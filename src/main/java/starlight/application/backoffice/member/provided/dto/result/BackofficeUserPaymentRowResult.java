package starlight.application.backoffice.member.provided.dto.result;

import starlight.domain.order.enumerate.OrderStatus;

import java.time.LocalDateTime;

public record BackofficeUserPaymentRowResult(
        Long orderId,
        String orderCode,
        String usageProductCode,
        Integer usageCount,
        Long price,
        OrderStatus orderStatus,
        String latestPaymentStatus,
        String paymentKey,
        LocalDateTime createdAt,
        LocalDateTime approvedAt
) {
    public static BackofficeUserPaymentRowResult of(
            Long orderId,
            String orderCode,
            String usageProductCode,
            Integer usageCount,
            Long price,
            OrderStatus orderStatus,
            String latestPaymentStatus,
            String paymentKey,
            LocalDateTime createdAt,
            LocalDateTime approvedAt
    ) {
        return new BackofficeUserPaymentRowResult(
                orderId,
                orderCode,
                usageProductCode,
                usageCount,
                price,
                orderStatus,
                latestPaymentStatus,
                paymentKey,
                createdAt,
                approvedAt
        );
    }
}
