package starlight.order.toss.application.usage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.order.toss.adapter.usage.persistence.UsageHistoryRepository;
import starlight.order.toss.adapter.usage.persistence.UsageWalletRepository;
import starlight.order.toss.application.usage.provided.UsageCreditPort;
import starlight.order.toss.domain.order.vo.Money;
import starlight.order.toss.domain.exception.OrderErrorType;
import starlight.order.toss.domain.exception.OrderException;
import starlight.order.toss.domain.wallet.UsageHistory;
import starlight.order.toss.domain.wallet.UsageWallet;

@Service
@RequiredArgsConstructor
@Transactional
public class UsageCreditService implements UsageCreditPort {

    private final UsageWalletRepository usageWalletRepository;
    private final UsageHistoryRepository usageHistoryRepository;

    @Override
    public void chargeForOrder(Long userId, Long orderId, int usageCount) {
        if (userId == null || usageCount <= 0) {
            throw new OrderException(OrderErrorType.INVALID_USAGE_COUNT);
        }

        // 1. 지갑 조회 or 생성
        UsageWallet wallet = usageWalletRepository.findByUserId(userId)
                .orElseGet(() -> usageWalletRepository.save(UsageWallet.init(userId)));

        // 2. 도메인 로직: 사용권 충전
        wallet.chargeAiReport(usageCount);
        usageWalletRepository.save(wallet);

        // 3. 이력 기록
        usageHistoryRepository.save(
                UsageHistory.charged(
                        userId,
                        usageCount,
                        wallet.getAiReportRemainingCount(),
                        orderId
                )
        );
    }
}
