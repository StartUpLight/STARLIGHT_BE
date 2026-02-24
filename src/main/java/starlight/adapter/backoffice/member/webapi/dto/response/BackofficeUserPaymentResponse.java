package starlight.adapter.backoffice.member.webapi.dto.response;

import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPaymentResult;

import java.util.List;

public record BackofficeUserPaymentResponse(
        BackofficeUserWalletResponse wallet,
        List<BackofficeUserPaymentRowResponse> payments,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeUserPaymentResponse from(BackofficeUserPaymentResult result) {
        return new BackofficeUserPaymentResponse(
                BackofficeUserWalletResponse.from(result.wallet()),
                result.payments().stream()
                        .map(BackofficeUserPaymentRowResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
