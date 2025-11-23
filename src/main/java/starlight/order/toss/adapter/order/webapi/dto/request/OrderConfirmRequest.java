package starlight.order.toss.adapter.order.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderConfirmRequest(
        @NotBlank
        String paymentKey,

        @NotBlank
        String orderCode
) { }
