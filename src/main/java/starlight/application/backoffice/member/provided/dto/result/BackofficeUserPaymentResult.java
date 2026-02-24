package starlight.application.backoffice.member.provided.dto.result;

import java.util.List;

public record BackofficeUserPaymentResult(
        BackofficeUserWalletResult wallet,
        List<BackofficeUserPaymentRowResult> payments,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeUserPaymentResult of(
            BackofficeUserWalletResult wallet,
            List<BackofficeUserPaymentRowResult> payments,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new BackofficeUserPaymentResult(wallet, payments, page, size, totalElements, totalPages, hasNext);
    }
}
