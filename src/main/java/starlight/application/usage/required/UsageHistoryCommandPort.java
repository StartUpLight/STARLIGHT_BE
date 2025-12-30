package starlight.application.usage.required;

import starlight.domain.order.wallet.UsageHistory;

public interface UsageHistoryCommandPort {

    UsageHistory save(UsageHistory usageHistory);
}
