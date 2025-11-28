package starlight.application.order.provided.dto;

import java.time.Instant;

public record PaymentHistoryItemDto(
        String productName,
        String paymentMethod,
        Long price,
        Instant paidAt,
        String receiptUrl
) {
    public static PaymentHistoryItemDto of(
            String productName,
            String paymentMethod,
            Long price,
            Instant paidAt,
            String receiptUrl
    ) {
        return new PaymentHistoryItemDto(
                productName,
                paymentMethod,
                price,
                paidAt,
                receiptUrl
        );
    }
}