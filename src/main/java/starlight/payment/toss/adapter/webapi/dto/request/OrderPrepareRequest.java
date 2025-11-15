package starlight.payment.toss.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderPrepareRequest(
        @NotBlank
        String orderCode,

        @NotNull
        Long price,

        @NotNull
        Long buyerId,

        @NotNull
        Long businessPlanId
) { }
