package starlight.application.backoffice.order.provided.dto.result;

import starlight.domain.order.enumerate.OrderStatus;

import java.time.LocalDateTime;

public record BackofficePaymentRowResult(
        Long orderId,
        String orderCode,
        Long userId,
        String userName,
        String userEmail,
        String usageProductCode,
        Integer usageCount,
        Long price,
        OrderStatus orderStatus,
        String latestPaymentStatus,
        LocalDateTime createdAt,
        LocalDateTime approvedAt,
        String paymentKey
) {
    public static BackofficePaymentRowResult of(
            Long orderId,
            String orderCode,
            Long userId,
            String userName,
            String userEmail,
            String usageProductCode,
            Integer usageCount,
            Long price,
            OrderStatus orderStatus,
            String latestPaymentStatus,
            LocalDateTime createdAt,
            LocalDateTime approvedAt,
            String paymentKey
    ) {
        return new BackofficePaymentRowResult(
                orderId,
                orderCode,
                userId,
                userName,
                userEmail,
                usageProductCode,
                usageCount,
                price,
                orderStatus,
                latestPaymentStatus,
                createdAt,
                approvedAt,
                paymentKey
        );
    }
}
