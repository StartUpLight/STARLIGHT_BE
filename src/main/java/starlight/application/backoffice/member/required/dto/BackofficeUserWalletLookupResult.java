package starlight.application.backoffice.member.required.dto;

public record BackofficeUserWalletLookupResult(
        Long chargedCount,
        Long usedCount,
        Long remainingCount
) {
    public static BackofficeUserWalletLookupResult of(Long chargedCount, Long usedCount, Long remainingCount) {
        return new BackofficeUserWalletLookupResult(chargedCount, usedCount, remainingCount);
    }
}
