    package starlight.payment.toss.domain;

    import io.jsonwebtoken.lang.Assert;
    import jakarta.persistence.*;
    import lombok.AccessLevel;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import starlight.payment.toss.domain.enumerate.PaymentStatus;

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
        private PaymentStatus status;

        @Column(length = 3, nullable = false)
        private String currency;

        @Column(nullable = false)
        private Long totalAmount;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<PaymentRecords> payments = new ArrayList<>();

        @Version
        private Long version;

        @Column(nullable = false, updatable = false)
        private Instant createdAt;

        @Column(nullable = false)
        private Instant updatedAt;

        // Orders
        // 주문상태: NEW("주문 생성됨 (결제 전)"), PAID("결제 완료"), CANCELED("주문/결제 취소");
        // 결제 완료(PAID) 상태에서만 주문/결제 취소(CANCELED)로 상태 변화 가능
        // 결제 완료(PAID) 상태에서는 주문/결제 취소 요청 가능
        // 주문 생성됨 (NEW) 상태에서만 결제 승인(PAID)으로 상태 변화 가능
        // 주문 생성됨 (NEW) 상태에서는 결제 요청 가능
        // 결제 취소(CANCELED) 상태에서는 더 이상 상태 변화 불가

        // 환불은 전체 금액에 대해서만 환불 가능하다 (부분 환불 불가)
        // 환불 요청 시, 주문 상태가 PAID여야 하며, 환불 완료 시 주문 상태를 CANCELED로 변경한다.

        /* ---------- 라이프사이클 ---------- */
        @PrePersist
        void prePersist() {
            Instant now = Instant.now();
            createdAt = now;
            updatedAt = now;
            if (status == null) status = PaymentStatus.NEW;
            if (currency == null) currency = "KRW";
        }

        @PreUpdate
        void preUpdate() {
            updatedAt = Instant.now();
        }

        /* ---------- 팩토리/도메인 메서드 ---------- */
        public static Orders newOrder(String orderCode, Long buyerId, Long businessPlanId, Long totalAmount) {
            Orders o = new Orders();
            o.orderCode = orderCode;
            o.buyerId = Objects.requireNonNull(buyerId, "buyerId");
            o.businessPlanId = businessPlanId;
            o.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount");
            o.status = PaymentStatus.NEW;
            o.currency = "KRW";
            return o;
        }

        /** 가장 최신 결제 시도 가져오기 (필요하면) */
        public Optional<PaymentRecords> latestPayment() {
            return payments.stream()
                    .max(Comparator.comparing(PaymentRecords::getCreatedAt));
        }

        public void markPaid() {
            this.status = PaymentStatus.PAID;
        }

        public void cancel() {
            Assert.isTrue(this.status == PaymentStatus.PAID, "결제 완료 상태에서만 취소 가능합니다.");
            this.status = PaymentStatus.CANCELED;
        }
    }
