package starlight.order.toss.adapter.usage.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.order.toss.domain.wallet.UsageWallet;

import java.util.Optional;

public interface UsageWalletRepository extends JpaRepository<UsageWallet, Long> {

    /**
     * userId 기준으로 지갑 조회
     * - 없으면 Optional.empty()
     */
    Optional<UsageWallet> findByUserId(Long userId);

    // 필요하면 나중에 이런 것도 추가 가능
    // boolean existsByUserId(Long userId);
}