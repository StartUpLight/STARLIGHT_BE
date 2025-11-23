package starlight.order.toss.adapter.order.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderPrepareRequest(
        @NotBlank
        String orderCode,

        @NotNull
        Long buyerId,

        @NotNull
        Long businessPlanId,

        @NotBlank
        String productCode
) { }
