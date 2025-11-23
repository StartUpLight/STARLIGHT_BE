package starlight.order.toss.adapter.usage.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.order.toss.domain.wallet.UsageHistory;

public interface UsageHistoryRepository extends JpaRepository<UsageHistory, Long> {
}
