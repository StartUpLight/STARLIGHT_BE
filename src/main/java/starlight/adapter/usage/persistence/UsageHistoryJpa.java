package starlight.adapter.usage.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.usage.required.UsageHistoryCommandPort;
import starlight.domain.order.wallet.UsageHistory;

@Repository
@RequiredArgsConstructor
public class UsageHistoryJpa implements UsageHistoryCommandPort {

    private final UsageHistoryRepository repository;

    @Override
    public UsageHistory save(UsageHistory usageHistory) {
        return repository.save(usageHistory);
    }
}
