package starlight.application.usage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.application.order.required.UsageCreditChargePort;
import starlight.application.usage.required.UsageHistoryCommandPort;
import starlight.application.usage.required.UsageWalletCommandPort;
import starlight.application.usage.required.UsageWalletQueryPort;
import starlight.domain.order.exception.OrderErrorType;
import starlight.domain.order.exception.OrderException;
import starlight.domain.order.wallet.UsageHistory;
import starlight.domain.order.wallet.UsageWallet;

@Service
@RequiredArgsConstructor
@Transactional
public class UsageCreditChargeService implements UsageCreditChargePort {

    private final UsageWalletQueryPort usageWalletQueryPort;
    private final UsageWalletCommandPort usageWalletCommandPort;
    private final UsageHistoryCommandPort usageHistoryCommandPort;

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
        UsageWallet wallet = usageWalletQueryPort.findByUserId(userId)
                .orElseGet(() -> usageWalletCommandPort.save(UsageWallet.init(userId)));

        // 사용권 충전
        wallet.chargeAiReport(usageCount);
        usageWalletCommandPort.save(wallet);

        // 이력 기록
        usageHistoryCommandPort.save(
                UsageHistory.charged(
                        userId,
                        usageCount,
                        wallet.getAiReportRemainingCount(),
                        orderId
                )
        );
    }
}
