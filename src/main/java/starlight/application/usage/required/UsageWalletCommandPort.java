package starlight.application.usage.required;

import starlight.domain.order.wallet.UsageWallet;

public interface UsageWalletCommandPort {

    UsageWallet save(UsageWallet usageWallet);
}
