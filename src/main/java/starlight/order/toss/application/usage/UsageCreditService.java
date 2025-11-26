package starlight.order.toss.application.usage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.order.toss.adapter.usage.persistence.UsageHistoryRepository;
import starlight.order.toss.adapter.usage.persistence.UsageWalletRepository;
import starlight.order.toss.application.usage.provided.UsageCreditPort;
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

    /**
     * 주문 결제가 완료되었을 때 사용권(지갑)을 충전한다.
     *
     * @param userId     주문자 ID
     * @param orderId    주문 PK (UsageHistory 연동용)
     * @param usageCount 몇 회권인지 (1회 / 2회 등)
     */
    @Override
    public void chargeForOrder(Long userId, Long orderId, int usageCount) {
        if (userId == null || usageCount <= 0) {
            throw new OrderException(OrderErrorType.INVALID_USAGE_COUNT);
        }

        // 지갑 조회 or 생성
        UsageWallet wallet = usageWalletRepository.findByUserId(userId)
                .orElseGet(() -> usageWalletRepository.save(UsageWallet.init(userId)));

        // 사용권 충전
        wallet.chargeAiReport(usageCount);
        usageWalletRepository.save(wallet);

        // 이력 기록
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
