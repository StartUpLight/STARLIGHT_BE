package starlight.payment.toss.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)          // ★ 1:N의 N 쪽
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

    @Column(name = "amount", nullable = false)
    private Long amount;

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

    // PaymentRecords에는 결제 수단에 대한 정보를 저장한다
    // (예: 카드, 간편결제 등) — ConfirmResponse의 method 필드와 매핑된다
    // provider 필드는 간편결제의 제공자(예: 카카오페이 등)를 저장한다

    /* ---------- 연관/도메인 메서드 ---------- */
    void bindOrder(Orders order) { this.order = order; }

    public static PaymentRecords requestedFor(Long amount) {
        PaymentRecords paymentRecords = new PaymentRecords();
        paymentRecords.pg = "TOSS";
        paymentRecords.status = "REQUESTED";
        paymentRecords.amount = amount;
        return paymentRecords;
    }

    public void markRequested(Long amount) {
        this.amount = amount;
        this.status = "REQUESTED";
    }

    public void markDone(String paymentKey, String method, String provider, String receiptUrl, Instant approvedAt) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.provider = provider;
        this.receiptUrl = receiptUrl;
        this.approvedAt = approvedAt != null ? approvedAt : Instant.now();
        this.status = "DONE";
    }

    public void markCanceled() {
        this.status = "CANCELED";
    }
}
