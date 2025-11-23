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
import starlight.shared.AbstractEntity;
import starlight.shared.BaseEntity;

import java.time.Instant;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends AbstractEntity {

    @Column(length = 64, nullable = false, unique = true)
    private String orderCode;

    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Column(length = 3, nullable = false)
    private String currency = "KRW";

    @Column(nullable = false)
    private Long price;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRecords> payments = new ArrayList<>();

    @Version
    private Long version;

    @Column(length = 50, nullable = false)
    private String usageProductCode;    // 어떤 상품코드로 샀는지

    @Column(nullable = false)
    private Integer usageCount;         // 몇 회권인지 (1 / 2)

    public static Orders newUsageOrder(OrderCode orderCode, Long buyerId, Money money, UsageProductType product) {
        Objects.requireNonNull(orderCode, "orderCode는 필수입니다.");
        Objects.requireNonNull(buyerId, "buyerId는 필수입니다.");
        Objects.requireNonNull(money, "money는 필수입니다.");
        Objects.requireNonNull(product, "product는 필수입니다.");

        Orders orders = new Orders();
        orders.orderCode = orderCode.getValue();
        orders.buyerId = buyerId;
        orders.price = money.getAmount();
        orders.currency = money.getCurrency();
        orders.usageProductCode = product.getCode();
        orders.usageCount = product.getUsageCount();
        orders.status = OrderStatus.NEW;
        return orders;
    }

    public void validateSameBuyer(Long buyerId) {
        if (!Objects.equals(this.buyerId, buyerId)) {
            throw new OrderException(OrderErrorType.ORDER_CODE_BUYER_MISMATCH);
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
    public void addPaymentAttempt(Money paymentMoney) {
        // 이미 결제 완료된 주문이면 시도 불가
        if (this.status == OrderStatus.PAID) {
            throw new OrderException(OrderErrorType.ALREADY_PAID);
        }

        // 주문 금액과 결제 금액이 일치해야 함
        Money orderMoney = Money.of(this.price, this.currency);
        if (!orderMoney.equals(paymentMoney)) {
            throw new OrderException(OrderErrorType.PAYMENT_AMOUNT_MISMATCH);
        }

        // 결제 시도 생성 및 추가
        PaymentRecords p = PaymentRecords.requestedFor(this, paymentMoney.getAmount());
        this.payments.add(p);
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
}