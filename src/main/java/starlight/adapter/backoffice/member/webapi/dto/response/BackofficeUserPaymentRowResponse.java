package starlight.adapter.backoffice.member.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPaymentRowResult;
import starlight.domain.order.enumerate.OrderStatus;

import java.time.LocalDateTime;

public record BackofficeUserPaymentRowResponse(
        Long orderId,
        String orderCode,
        String usageProductCode,
        Integer usageCount,
        Long price,
        OrderStatus orderStatus,
        String latestPaymentStatus,
        String paymentKey,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime approvedAt
) {
    public static BackofficeUserPaymentRowResponse from(BackofficeUserPaymentRowResult result) {
        return new BackofficeUserPaymentRowResponse(
                result.orderId(),
                result.orderCode(),
                result.usageProductCode(),
                result.usageCount(),
                result.price(),
                result.orderStatus(),
                result.latestPaymentStatus(),
                result.paymentKey(),
                result.createdAt(),
                result.approvedAt()
        );
    }
}
