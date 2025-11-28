package starlight.domain.order.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.order.exception.OrderErrorType;
import starlight.domain.order.exception.OrderException;
import starlight.shared.AbstractEntity;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageWallet extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Integer aiReportChargedCount;   // AI 리포트: 지금까지 충전된 총 횟수

    @Column(nullable = false)
    private Integer aiReportUsedCount;       // AI 리포트: 지금까지 사용된 총 횟수

    @Version
    private Long version;

    /**
     * 지갑 초기화
     */
    public static UsageWallet init(Long userId) {
        UsageWallet wallet = new UsageWallet();
        wallet.userId = Objects.requireNonNull(userId, "userId는 필수입니다.");
        wallet.aiReportChargedCount = 0;
        wallet.aiReportUsedCount = 0;
        return wallet;
    }

    /**
     * AI 리포트 횟수 충전
     */
    public void chargeAiReport(int count) {
        if (count <= 0) {
            throw new OrderException(OrderErrorType.INVALID_USAGE_COUNT);
        }
        this.aiReportChargedCount += count;
    }

    /**
     * 남은 AI 리포트 사용 가능 횟수
     */
    public int getAiReportRemainingCount() {
        return aiReportChargedCount - aiReportUsedCount;
    }

    /**
     * AI 리포트 사용
     */
    public void useAiReport(int count) {
        if (count <= 0) {
            throw new OrderException(OrderErrorType.INVALID_USAGE_COUNT);
        }
        if (getAiReportRemainingCount() < count) {
            throw new OrderException(OrderErrorType.INSUFFICIENT_AI_REPORT_BALANCE);
        }
        this.aiReportUsedCount += count;
    }
}
