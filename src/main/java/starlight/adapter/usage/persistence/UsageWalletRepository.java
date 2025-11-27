package starlight.adapter.usage.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.order.wallet.UsageWallet;

import java.util.Optional;

public interface UsageWalletRepository extends JpaRepository<UsageWallet, Long> {

    Optional<UsageWallet> findByUserId(Long userId);
}