package starlight.order.toss.adapter.webapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public record TossClientResponse (
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancel(
            // 기본 정보
            String mId,                 // 가맹점 ID
            String lastTransactionKey,  // 마지막 거래 키
            String paymentKey,          // 결제 키
            String orderId,             // ORD20251113-3PNGMYUX
            String orderName,           // starlight 크레딧 5,000원
            Integer taxExemptionAmount, // 비과세 금액
            String status,              // (String) CANCELED
            String requestedAt,         // 요청 시각
            String approvedAt,          // 승인 시각

            // 취소 내역
            List<CancelDetail> cancels,

            // 결제 정보
            String secret,              // 가맹점 시크릿 코드 (ps_kYG57Eba3GKvEgqK6GnE3pWDOxmA)
            String type,                // 결제 타입 ("NORMAL")
            EasyPay easyPay,

            // 영수증
            Receipt receipt,

            // 금액 정보
            String currency,            // "KRW"
            Integer totalAmount,        // 5000
            Integer balanceAmount,      // 0
            Integer suppliedAmount,     // 5000
            Integer vat,                // 455
            Integer taxFreeAmount,      // 0
            String method               // "간편결제"
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record CancelDetail(
                String cancelReason,    // "user_request"
                String canceledAt,      // "2025-11-13T19:09:04+09:00"
                String cancelStatus,    // "DONE"
                Integer cancelAmount    // 5000
        ) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record EasyPay(
                String provider,        // "카카오페이"
                Integer amount,         // 5000
                Integer discountAmount  // 0
        ) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Receipt(
                String url              // 영수증 URL
        ) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Confirm(
            String paymentKey,
            String orderId,
            String status,           // e.g., DONE
            String method,           // CARD/EASY_PAY...
            Long totalAmount,
            OffsetDateTime approvedAt,
            EasyPay easyPay,
            Receipt receipt
    ) {
        public record EasyPay(String provider) {}
        public record Receipt(String url) {}

        public String providerOrNull() {
            return (easyPay != null) ? easyPay.provider() : null;
        }

        public String receiptUrlOrNull() {
            return (receipt != null) ? receipt.url() : null;
        }

        public Instant approvedAtOrNow() {
            return (approvedAt != null) ? approvedAt.toInstant() : Instant.now();
        }
    }
}
