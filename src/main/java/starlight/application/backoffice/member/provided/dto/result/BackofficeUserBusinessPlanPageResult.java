package starlight.application.backoffice.member.provided.dto.result;

import java.util.List;

public record BackofficeUserBusinessPlanPageResult(
        List<BackofficeUserBusinessPlanRowResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeUserBusinessPlanPageResult of(
            List<BackofficeUserBusinessPlanRowResult> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new BackofficeUserBusinessPlanPageResult(content, page, size, totalElements, totalPages, hasNext);
    }
}
