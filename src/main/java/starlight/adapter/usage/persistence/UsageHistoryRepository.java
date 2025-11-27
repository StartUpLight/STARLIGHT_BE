package starlight.adapter.usage.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.order.wallet.UsageHistory;

public interface UsageHistoryRepository extends JpaRepository<UsageHistory, Long> {
}
