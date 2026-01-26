package starlight.application.backoffice.expert.provided.dto.input;

import starlight.domain.expert.enumerate.ExpertActiveStatus;

public record BackofficeExpertActiveStatusUpdateInput(
        Long expertId,
        ExpertActiveStatus activeStatus
) {
    public static BackofficeExpertActiveStatusUpdateInput of(Long expertId, ExpertActiveStatus activeStatus) {
        return new BackofficeExpertActiveStatusUpdateInput(expertId, activeStatus);
    }
}
