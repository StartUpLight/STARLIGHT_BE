package starlight.adapter.usage.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.usage.provided.UsageHistoryQuery;
import starlight.domain.order.wallet.UsageHistory;

@Repository
@RequiredArgsConstructor
public class UsageHistoryRepositoryJpa implements UsageHistoryQuery {

    private final UsageHistoryRepository repository;

    @Override
    public UsageHistory save(UsageHistory usageHistory){
        return repository.save(usageHistory);
    }
}