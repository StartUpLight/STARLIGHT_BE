package starlight.application.usage.required;

import starlight.domain.order.wallet.UsageWallet;

import java.util.Optional;

public interface UsageWalletQueryPort {

    Optional<UsageWallet> findByUserId(Long userId);
}
