package starlight.order.toss.adapter.usage.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.order.toss.domain.wallet.UsageHistory;

public interface UsageHistoryRepository extends JpaRepository<UsageHistory, Long> {

    // 지금은 save(...)만 쓰니까 커스텀 메서드가 꼭 필요하진 않음.
    // 나중에 "유저의 사용 이력 조회" 같은 요구가 생기면 여기에 추가하면 돼.

    // List<UsageHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
