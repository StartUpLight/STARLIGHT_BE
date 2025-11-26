package starlight.domain.order.order;

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
public class PaymentRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(length = 20, nullable = false)
    private String pg;

    @Column(length = 128, unique = true)
    private String paymentKey;

    @Column(length = 40)
    private String method;

    @Column(length = 40)
    private String provider;

    @Column(nullable = false)
    private Long price;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(length = 255)
    private String receiptUrl;

    private Instant approvedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (status == null) status = "REQUESTED";
        if (pg == null) pg = "TOSS";
    }

    /**
     * 결제 요청 생성
     * @param order 연관된 주문
     * @param amount 결제 금액
     * @return 생성된 PaymentRecords
     */
    public static PaymentRecords requestedFor(Orders order, Long amount) {
        Objects.requireNonNull(order, "order는 필수입니다.");
        Objects.requireNonNull(amount, "amount는 필수입니다.");

        PaymentRecords payment = new PaymentRecords();
        payment.order = order;
        payment.pg = "TOSS";
        payment.status = "REQUESTED";
        payment.price = amount;

        return payment;
    }

    /**
     * 결제 승인 완료 처리
     */
    public void markDone(String paymentKey, String method, String provider,
                         String receiptUrl, Instant approvedAt) {

        validateForCompletion(paymentKey);

        this.paymentKey = paymentKey;
        this.method = method;
        this.provider = provider;
        this.receiptUrl = receiptUrl;
        this.approvedAt = approvedAt != null ? approvedAt : Instant.now();
        this.status = "DONE";
    }

    /**
     * 결제 취소 처리
     */
    public void markCanceled() {
        if (!"DONE".equals(this.status)) {
            throw new IllegalStateException(
                    "DONE 상태에서만 취소 가능합니다. 현재 상태: " + this.status
            );
        }
        this.status = "CANCELED";
    }

    /**
     * 결제키가 없으면 PG 취소 불가능
     */
    public void ensureHasPaymentKey() {
        if (this.paymentKey == null || this.paymentKey.trim().isEmpty()) {
            throw new OrderException(OrderErrorType.NO_PAYMENT_KEY);
        }
    }

    private void validateForCompletion(String paymentKey) {
        if (!"REQUESTED".equals(this.status)) {
            throw new IllegalStateException(
                    "REQUESTED 상태에서만 승인 가능합니다. 현재 상태: " + this.status
            );
        }
        if (paymentKey == null || paymentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("paymentKey는 필수입니다.");
        }
    }
}