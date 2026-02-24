package starlight.application.backoffice.order.provided.dto.result;

import java.util.List;

public record BackofficePaymentPageResult(
        List<BackofficePaymentRowResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficePaymentPageResult of(
            List<BackofficePaymentRowResult> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new BackofficePaymentPageResult(content, page, size, totalElements, totalPages, hasNext);
    }
}
