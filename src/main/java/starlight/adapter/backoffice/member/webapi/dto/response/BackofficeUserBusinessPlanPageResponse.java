package starlight.adapter.backoffice.member.webapi.dto.response;

import starlight.application.backoffice.member.provided.dto.result.BackofficeUserBusinessPlanPageResult;

import java.util.List;

public record BackofficeUserBusinessPlanPageResponse(
        List<BackofficeUserBusinessPlanRowResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeUserBusinessPlanPageResponse from(BackofficeUserBusinessPlanPageResult result) {
        return new BackofficeUserBusinessPlanPageResponse(
                result.content().stream()
                        .map(BackofficeUserBusinessPlanRowResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
