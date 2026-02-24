package starlight.application.backoffice.order.required.dto;

import starlight.domain.order.enumerate.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record BackofficePaymentOrderLookupResult(
        Long orderId,
        String orderCode,
        Long userId,
        String usageProductCode,
        Integer usageCount,
        Long price,
        OrderStatus orderStatus,
        LocalDateTime createdAt,
        String latestPaymentStatus,
        String paymentKey,
        Instant approvedAt
) {
    public static BackofficePaymentOrderLookupResult of(
            Long orderId,
            String orderCode,
            Long userId,
            String usageProductCode,
            Integer usageCount,
            Long price,
            OrderStatus orderStatus,
            LocalDateTime createdAt,
            String latestPaymentStatus,
            String paymentKey,
            Instant approvedAt
    ) {
        return new BackofficePaymentOrderLookupResult(
                orderId,
                orderCode,
                userId,
                usageProductCode,
                usageCount,
                price,
                orderStatus,
                createdAt,
                latestPaymentStatus,
                paymentKey,
                approvedAt
        );
    }
}
