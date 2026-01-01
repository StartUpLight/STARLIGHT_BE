package starlight.application.order.provided.dto;

import java.time.Instant;

public record PaymentHistoryItemResult(
        String productName,
        String paymentMethod,
        Long price,
        Instant paidAt,
        String receiptUrl
) {
    public static PaymentHistoryItemResult of(
            String productName,
            String paymentMethod,
            Long price,
            Instant paidAt,
            String receiptUrl
    ) {
        return new PaymentHistoryItemResult(
                productName,
                paymentMethod,
                price,
                paidAt,
                receiptUrl
        );
    }
}
