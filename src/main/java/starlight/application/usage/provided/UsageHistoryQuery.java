package starlight.application.usage.provided;

import starlight.domain.order.wallet.UsageHistory;

public interface UsageHistoryQuery {

    UsageHistory save(UsageHistory usageHistory);
}
