package starlight.adapter.usage.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.usage.provided.UsageWalletQuery;
import starlight.domain.order.wallet.UsageWallet;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsageWalletRepositoryJpa implements UsageWalletQuery {

    private final UsageWalletRepository repository;

    @Override
    public Optional<UsageWallet> findByUserId(Long userId){
        return repository.findByUserId(userId);
    }

    @Override
    public UsageWallet save(UsageWallet usageWallet){
        return repository.save(usageWallet);
    }
}
