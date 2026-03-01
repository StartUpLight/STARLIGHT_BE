package starlight.application.backoffice.member.provided.dto.result;

public record BackofficeUserWalletResult(
        Long chargedCount,
        Long usedCount,
        Long remainingCount
) {
    public static BackofficeUserWalletResult of(Long chargedCount, Long usedCount, Long remainingCount) {
        return new BackofficeUserWalletResult(chargedCount, usedCount, remainingCount);
    }
}
