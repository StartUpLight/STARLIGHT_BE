package starlight.application.backoffice.businessplan.provided.dto.result;

import java.util.List;

public record BackofficeBusinessPlanPageResult(
        List<BackofficeBusinessPlanRowResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeBusinessPlanPageResult of(
            List<BackofficeBusinessPlanRowResult> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new BackofficeBusinessPlanPageResult(content, page, size, totalElements, totalPages, hasNext);
    }
}
