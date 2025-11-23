package starlight.order.toss.adapter.order.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderPrepareRequest(
        @NotBlank
        String orderCode,

        @NotBlank
        String productCode
) { }
