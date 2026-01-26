package starlight.adapter.backoffice.expert.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BackofficeExpertProfileImageUpdateRequest(
        @NotBlank String profileImageUrl
) { }
