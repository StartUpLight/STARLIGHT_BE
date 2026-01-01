package starlight.adapter.usage.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.usage.required.UsageWalletCommandPort;
import starlight.application.usage.required.UsageWalletQueryPort;
import starlight.domain.order.wallet.UsageWallet;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsageWalletJpa implements UsageWalletQueryPort, UsageWalletCommandPort {

    private final UsageWalletRepository repository;

    @Override
    public Optional<UsageWallet> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public UsageWallet save(UsageWallet usageWallet) {
        return repository.save(usageWallet);
    }
}
