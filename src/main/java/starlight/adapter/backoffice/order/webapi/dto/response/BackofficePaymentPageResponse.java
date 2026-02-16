package starlight.adapter.backoffice.order.webapi.dto.response;

import starlight.application.backoffice.order.provided.dto.result.BackofficePaymentPageResult;

import java.util.List;

public record BackofficePaymentPageResponse(
        List<BackofficePaymentRowResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficePaymentPageResponse from(BackofficePaymentPageResult result) {
        return new BackofficePaymentPageResponse(
                result.content().stream()
                        .map(BackofficePaymentRowResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
