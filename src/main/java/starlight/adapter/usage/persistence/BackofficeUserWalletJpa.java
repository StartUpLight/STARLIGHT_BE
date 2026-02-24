package starlight.adapter.usage.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.member.required.BackofficeUserWalletLookupPort;
import starlight.application.backoffice.member.required.dto.BackofficeUserWalletLookupResult;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BackofficeUserWalletJpa implements BackofficeUserWalletLookupPort {

    private final UsageWalletRepository usageWalletRepository;

    @Override
    public Optional<BackofficeUserWalletLookupResult> findWalletByUserId(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        return usageWalletRepository.findByUserId(userId)
                .map(wallet -> BackofficeUserWalletLookupResult.of(
                        wallet.getAiReportChargedCount().longValue(),
                        wallet.getAiReportUsedCount().longValue(),
                        (long) wallet.getAiReportRemainingCount()
                ));
    }
}
