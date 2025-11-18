package starlight.order.toss.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderCancelRequest(
        @NotBlank(message = "orderId는 필수입니다.")
        String orderCode,

        @NotBlank(message = "취소 사유는 필수입니다.")
        String reason
) { }
