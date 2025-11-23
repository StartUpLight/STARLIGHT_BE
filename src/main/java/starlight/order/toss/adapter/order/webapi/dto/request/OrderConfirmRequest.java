package starlight.order.toss.adapter.order.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import jakarta.validation.constraints.NotBlank;

public record OrderConfirmRequest(
        @NotBlank
        String paymentKey,

        @NotBlank
        String orderCode
) { }
