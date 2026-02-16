package starlight.adapter.backoffice.order.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.backoffice.order.provided.dto.result.BackofficePaymentRowResult;
import starlight.domain.order.enumerate.OrderStatus;

import java.time.LocalDateTime;

public record BackofficePaymentRowResponse(
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime approvedAt,
        String paymentKey
) {
    public static BackofficePaymentRowResponse from(BackofficePaymentRowResult result) {
        return new BackofficePaymentRowResponse(
                result.orderId(),
                result.orderCode(),
                result.userId(),
                result.userName(),
                result.userEmail(),
                result.usageProductCode(),
                result.usageCount(),
                result.price(),
                result.orderStatus(),
                result.latestPaymentStatus(),
                result.createdAt(),
                result.approvedAt(),
                result.paymentKey()
        );
    }
}
