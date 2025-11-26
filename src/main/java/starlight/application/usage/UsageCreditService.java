package starlight.application.usage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.application.usage.provided.UsageCreditPort;
import starlight.application.usage.provided.UsageHistoryQuery;
import starlight.application.usage.provided.UsageWalletQuery;
import starlight.domain.order.exception.OrderErrorType;
import starlight.domain.order.exception.OrderException;
import starlight.domain.order.wallet.UsageHistory;
import starlight.domain.order.wallet.UsageWallet;

@Service
@RequiredArgsConstructor
@Transactional
public class UsageCreditService implements UsageCreditPort {

    private final UsageWalletQuery usageWalletQuery;
    private final UsageHistoryQuery usageHistoryQuery;

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
        UsageWallet wallet = usageWalletQuery.findByUserId(userId)
                .orElseGet(() -> usageWalletQuery.save(UsageWallet.init(userId)));

        // 사용권 충전
        wallet.chargeAiReport(usageCount);
        usageWalletQuery.save(wallet);

        // 이력 기록
        usageHistoryQuery.save(
                UsageHistory.charged(
                        userId,
                        usageCount,
                        wallet.getAiReportRemainingCount(),
                        orderId
                )
        );
    }
}
