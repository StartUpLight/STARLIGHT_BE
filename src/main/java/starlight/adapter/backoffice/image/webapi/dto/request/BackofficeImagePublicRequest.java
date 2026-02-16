package starlight.adapter.backoffice.image.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BackofficeImagePublicRequest(
        @NotBlank String objectUrl
) { }
