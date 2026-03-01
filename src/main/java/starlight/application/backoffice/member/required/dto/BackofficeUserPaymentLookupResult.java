package starlight.application.backoffice.member.required.dto;

import starlight.domain.order.enumerate.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record BackofficeUserPaymentLookupResult(
        Long orderId,
        String orderCode,
        String usageProductCode,
        Integer usageCount,
        Long price,
        OrderStatus orderStatus,
        String latestPaymentStatus,
        String paymentKey,
        LocalDateTime createdAt,
        Instant approvedAt
) {
    public static BackofficeUserPaymentLookupResult of(
            Long orderId,
            String orderCode,
            String usageProductCode,
            Integer usageCount,
            Long price,
            OrderStatus orderStatus,
            String latestPaymentStatus,
            String paymentKey,
            LocalDateTime createdAt,
            Instant approvedAt
    ) {
        return new BackofficeUserPaymentLookupResult(
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
