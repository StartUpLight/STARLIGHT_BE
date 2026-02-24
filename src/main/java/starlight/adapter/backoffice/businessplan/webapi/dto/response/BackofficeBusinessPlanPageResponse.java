package starlight.adapter.backoffice.businessplan.webapi.dto.response;

import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanPageResult;

import java.util.List;

public record BackofficeBusinessPlanPageResponse(
        List<BackofficeBusinessPlanRowResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeBusinessPlanPageResponse from(BackofficeBusinessPlanPageResult result) {
        return new BackofficeBusinessPlanPageResponse(
                result.content().stream().map(BackofficeBusinessPlanRowResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
