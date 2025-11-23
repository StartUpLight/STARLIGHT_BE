package starlight.order.toss.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.order.toss.domain.enumerate.UsageProductType;
import starlight.order.toss.domain.order.vo.Money;
import starlight.order.toss.domain.order.vo.OrderCode;
import starlight.order.toss.domain.enumerate.OrderStatus;
import starlight.order.toss.domain.exception.OrderErrorType;
import starlight.order.toss.domain.exception.OrderException;

import java.time.Instant;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false, unique = true)
    private String orderCode;

    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerId;

    @Column(name = "business_plan_id")
    private Long businessPlanId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(nullable = false)
    private Long price;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRecords> payments = new ArrayList<>();

    @Version
    private Long version;

    // 어떤 상품코드로 샀는지
    @Column(length = 50, nullable = false)
    private String usageProductCode;

    // 몇 회권인지 (1 / 2)
    @Column(nullable = false)
    private Integer usageCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /* ---------- 라이프사이클 ---------- */
    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = OrderStatus.NEW;
        if (currency == null) currency = "KRW";
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    /* ---------- 팩토리 메서드 ---------- */

    public static Orders newOrder(OrderCode orderCode, Long buyerId, Long businessPlanId, Money money, UsageProductType product) {
        Objects.requireNonNull(orderCode, "orderCode는 필수입니다.");
        Objects.requireNonNull(buyerId, "buyerId는 필수입니다.");
        Objects.requireNonNull(money, "money는 필수입니다.");

        Orders orders = new Orders();
        orders.orderCode = orderCode.getValue();
        orders.buyerId = buyerId;
        orders.businessPlanId = businessPlanId;
        orders.price = money.getAmount();
        orders.currency = money.getCurrency();
        orders.usageProductCode = product.getCode();
        orders.usageCount = product.getUsageCount();
        orders.status = OrderStatus.NEW;
        return orders;
    }

    /* ---------- 도메인 로직 (불변식 보호) ---------- */

    /**
     * 같은 비즈니스 주문인지 검증
     * orderCode 재사용 시 호출
     */
    public void validateSameBusinessOrder(Long buyerId, Long businessPlanId) {
        if (!Objects.equals(this.buyerId, buyerId)) {
            throw new OrderException(OrderErrorType.ORDER_CODE_BUYER_MISMATCH);
        }
        if (!Objects.equals(this.businessPlanId, businessPlanId)) {
            throw new OrderException(OrderErrorType.ORDER_CODE_BUSINESS_PLAN_MISMATCH);
        }
    }

    public void validateSameProduct(UsageProductType product) {
        if (!Objects.equals(this.usageProductCode, product.getCode())) {
            throw new OrderException(OrderErrorType.ORDER_PRODUCT_MISMATCH);
        }
    }

    /**
     * 결제 시도 추가
     * - 이미 결제 완료된 주문이면 예외
     * - 주문 금액과 다른 금액이면 예외
     */
    public PaymentRecords addPaymentAttempt(Money paymentMoney) {
        // 1. 이미 결제 완료된 주문이면 시도 불가
        if (this.status == OrderStatus.PAID) {
            throw new OrderException(OrderErrorType.ALREADY_PAID);
        }

        // 2. 주문 금액과 결제 금액이 일치해야 함
        Money orderMoney = Money.of(this.price, this.currency);
        if (!orderMoney.equals(paymentMoney)) {
            throw new OrderException(OrderErrorType.PAYMENT_AMOUNT_MISMATCH);
        }

        // 3. 결제 시도 생성 및 추가
        PaymentRecords p = PaymentRecords.requestedFor(this, paymentMoney.getAmount());
        this.payments.add(p);

        return p;
    }

    /**
     * 결제 승인 처리
     * - NEW 상태에서만 가능
     */
    public void markPaid() {
        if (this.status == OrderStatus.PAID) {
            throw new OrderException(OrderErrorType.ALREADY_PAID);
        }
        if (this.status != OrderStatus.NEW) {
            throw new OrderException(OrderErrorType.INVALID_ORDER_STATE_FOR_PAYMENT);
        }
        this.status = OrderStatus.PAID;
    }

    /**
     * 주문/결제 취소
     * - PAID 상태에서만 가능
     */
    public void cancel() {
        if (this.status != OrderStatus.PAID) {
            throw new OrderException(OrderErrorType.INVALID_ORDER_STATE_FOR_CANCEL);
        }
        this.status = OrderStatus.CANCELED;
    }

    /* ---------- 조회 메서드 ---------- */

    /**
     * 가장 최근 결제 시도 조회 (Optional)
     */
    public Optional<PaymentRecords> latestPayment() {
        return payments.stream()
                .max(Comparator.comparing(PaymentRecords::getCreatedAt));
    }

    /**
     * 가장 최근 REQUESTED 상태 결제 시도 조회
     */
    public PaymentRecords getLatestRequestedOrThrow() {
        return payments.stream()
                .filter(p -> "REQUESTED".equals(p.getStatus()))
                .max(Comparator.comparing(PaymentRecords::getCreatedAt))
                .orElseThrow(() -> new OrderException(OrderErrorType.NO_REQUESTED_PAYMENT));
    }

    /**
     * 가장 최근 DONE 상태 결제 시도 조회
     */
    public PaymentRecords getLatestDoneOrThrow() {
        return payments.stream()
                .filter(p -> "DONE".equals(p.getStatus()))
                .max(Comparator.comparing(p ->
                        p.getApprovedAt() != null ? p.getApprovedAt() : p.getCreatedAt()
                ))
                .orElseThrow(() -> new OrderException(OrderErrorType.NO_DONE_PAYMENT));
    }

    /**
     * 가장 최근 결제 이력 조회 (승인시간 기준)
     */
    public PaymentRecords getLatestPaymentOrThrow() {
        return payments.stream()
                .max(Comparator.comparing(p ->
                        p.getApprovedAt() != null ? p.getApprovedAt() : p.getCreatedAt()
                ))
                .orElseThrow(() -> new OrderException(OrderErrorType.NO_DONE_PAYMENT));
    }

    /* ---------- 편의 메서드 ---------- */

    public Money getMoney() {
        return Money.of(this.price, this.currency);
    }

    public OrderCode getOrderCodeVO() {
        return OrderCode.of(this.orderCode);
    }
}

// Orders
// 주문상태: NEW("주문 생성됨 (결제 전)"), PAID("결제 완료"), CANCELED("주문/결제 취소");
// 결제 완료(PAID) 상태에서만 주문/결제 취소(CANCELED)로 상태 변화 가능
// 결제 완료(PAID) 상태에서는 주문/결제 취소 요청 가능
// 주문 생성됨 (NEW) 상태에서만 결제 승인(PAID)으로 상태 변화 가능
// 주문 생성됨 (NEW) 상태에서는 결제 요청 가능
// 결제 취소(CANCELED) 상태에서는 더 이상 상태 변화 불가

// 환불은 전체 금액에 대해서만 환불 가능하다 (부분 환불 불가)
// 환불 요청 시, 주문 상태가 PAID여야 하며, 환불 완료 시 주문 상태를 CANCELED로 변경한다.
/* ---------- 도메인 로직 (불변식 보호) ---------- */