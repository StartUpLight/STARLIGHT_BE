package starlight.adapter.backoffice.expert.webapi.dto.request;

import jakarta.validation.constraints.NotNull;
import starlight.domain.expert.enumerate.ExpertActiveStatus;

public record BackofficeExpertActiveStatusUpdateRequest(
        @NotNull ExpertActiveStatus activeStatus
) { }
