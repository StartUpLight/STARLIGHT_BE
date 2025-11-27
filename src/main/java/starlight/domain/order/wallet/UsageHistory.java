package starlight.domain.order.wallet;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.order.exception.OrderErrorType;
import starlight.domain.order.exception.OrderException;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageHistory {

    private static final String TYPE_CHARGE = "CHARGE";
    private static final String TYPE_USE = "USE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;            // 누가

    @Column
    private Long businessPlanId;    // 어떤 사업계획서에서 (사용 시에만 채움, 충전은 null 가능)

    @Column(nullable = false)
    private String type;            // CHARGE / USE 등

    @Column(nullable = false)
    private Integer amount;         // 이번에 증감된 횟수 (충전: +10, 사용: -1 이런 느낌)

    @Column(nullable = false)
    private Integer balanceAfter;   // 이 이벤트 이후 지갑 잔여 횟수

    @Column
    private Long orderId;           // 이 충전이 어느 주문에서 온 건지 대략 연결하고 싶으면

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    /**
     * 충전 이력 기록
     */
    public static UsageHistory charged(Long userId, int amount, int balanceAfter, Long orderId) {
        Objects.requireNonNull(userId, "userId는 필수입니다.");

        if (amount <= 0) {
            throw new OrderException(OrderErrorType.INVALID_USAGE_COUNT);
        }

        UsageHistory history = new UsageHistory();
        history.userId = userId;
        history.type = TYPE_CHARGE;
        history.amount = amount;          // 충전은 양수 그대로
        history.balanceAfter = balanceAfter;
        history.orderId = orderId;        // 없으면 null

        return history;
    }

    /**
     * 사용 이력 기록
     */
    public static UsageHistory used(Long userId, Long businessPlanId, int amount, int balanceAfter) {
        Objects.requireNonNull(userId, "userId는 필수입니다.");
        Objects.requireNonNull(businessPlanId, "businessPlanId는 필수입니다.");

        if (amount <= 0) {
            throw new OrderException(OrderErrorType.INVALID_USAGE_COUNT);
        }

        UsageHistory history = new UsageHistory();
        history.userId = userId;
        history.businessPlanId = businessPlanId;
        history.type = TYPE_USE;
        history.amount = -Math.abs(amount);  // 사용은 항상 음수로 저장
        history.balanceAfter = balanceAfter;

        return history;
    }
}
