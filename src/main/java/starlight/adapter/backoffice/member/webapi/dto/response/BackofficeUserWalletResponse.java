package starlight.adapter.backoffice.member.webapi.dto.response;

import starlight.application.backoffice.member.provided.dto.result.BackofficeUserWalletResult;

public record BackofficeUserWalletResponse(
        Long chargedCount,
        Long usedCount,
        Long remainingCount
) {
    public static BackofficeUserWalletResponse from(BackofficeUserWalletResult result) {
        return new BackofficeUserWalletResponse(
                result.chargedCount(),
                result.usedCount(),
                result.remainingCount()
        );
    }
}
