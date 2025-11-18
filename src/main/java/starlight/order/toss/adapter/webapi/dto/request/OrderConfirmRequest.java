package starlight.order.toss.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderConfirmRequest(
        @NotBlank
        String paymentKey,

        @NotBlank
        String orderCode,

        @NotNull @Positive
        Long price
) { }
