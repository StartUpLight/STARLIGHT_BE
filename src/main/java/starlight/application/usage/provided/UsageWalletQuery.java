package starlight.application.usage.provided;

import starlight.domain.order.wallet.UsageWallet;

import java.util.Optional;

public interface UsageWalletQuery {

    Optional<UsageWallet> findByUserId(Long userId);

    UsageWallet save(UsageWallet usageWallet);
}
