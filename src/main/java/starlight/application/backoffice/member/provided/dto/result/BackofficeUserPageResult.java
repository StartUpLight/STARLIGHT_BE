package starlight.application.backoffice.member.provided.dto.result;

import java.util.List;

public record BackofficeUserPageResult(
        List<BackofficeUserRowResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeUserPageResult of(
            List<BackofficeUserRowResult> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new BackofficeUserPageResult(content, page, size, totalElements, totalPages, hasNext);
    }
}
