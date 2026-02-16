package starlight.adapter.backoffice.expert.webapi.dto.response;

import starlight.application.backoffice.expert.provided.dto.result.BackofficeExpertCreateResult;

public record BackofficeExpertCreateResponse(
        Long id
) {
    public static BackofficeExpertCreateResponse from(BackofficeExpertCreateResult result) {
        return new BackofficeExpertCreateResponse(result.id());
    }
}
