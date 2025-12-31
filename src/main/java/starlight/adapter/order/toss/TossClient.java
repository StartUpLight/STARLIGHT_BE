package starlight.adapter.order.toss;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import starlight.application.order.provided.dto.TossClientResponse;
import starlight.application.order.required.PaymentGatewayPort;
import starlight.domain.order.exception.OrderErrorType;
import starlight.domain.order.exception.OrderException;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class TossClient implements PaymentGatewayPort {

    private final RestClient restClient;

    public TossClient(@Qualifier("tossRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public TossClientResponse.Confirm confirm(String orderCode, String paymentKey, Long price) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId",    orderCode,
                "amount",     price
        );
        try {
            TossClientResponse.Confirm response = restClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", orderCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TossClientResponse.Confirm.class);

            // 응답 검증
            validateConfirmResponse(response, orderCode, price);

            return response;

        } catch (Exception e) {
            log.error("토스 결제 승인 요청 중 에러 발생. orderId: {}, paymentKey: {}, message: {}", orderCode, paymentKey, e.getMessage(), e);
            throw new OrderException(OrderErrorType.TOSS_CLIENT_CONFIRM_ERROR);
        }
    }

    @Override
    public TossClientResponse.Cancel cancel(String paymentKey, String reason) {
        Map<String, Object> body = Map.of(
                "cancelReason", reason != null ? reason : "user_request"
        );
        try {
            TossClientResponse.Cancel response = restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TossClientResponse.Cancel.class);

            // 응답 검증
            validateCancelResponse(response);

            return response;

        } catch (Exception e) {
            log.error("토스 환불 요청 중 에러발생: {}", e.getMessage(), e);
            log.error("paymentKey: {}, reason: {}", paymentKey, reason);
            throw new OrderException(OrderErrorType.TOSS_CLIENT_CANCEL_ERROR);
        }

    }

    /**
     * confirm 응답 검증
     */
    private void validateConfirmResponse(TossClientResponse.Confirm response, String expectedOrderId, Long expectedAmount) {
        if (response == null) {
            throw new IllegalStateException("PG 응답이 null입니다.");
        }

        if (!Objects.equals(response.orderId(), expectedOrderId)) {
            throw new IllegalStateException(
                    String.format("PG 응답의 주문번호가 일치하지 않습니다. 예상: %s, 실제: %s",
                            expectedOrderId, response.orderId())
            );
        }

        if (!Objects.equals(response.totalAmount(), expectedAmount)) {
            throw new IllegalStateException(
                    String.format("PG 응답 금액이 주문 금액과 일치하지 않습니다. 예상: %d, 실제: %d",
                            expectedAmount, response.totalAmount())
            );
        }

        if (!"DONE".equals(response.status())) {
            throw new IllegalStateException(
                    "PG 응답 상태가 완료(DONE)가 아닙니다. status=" + response.status()
            );
        }
    }

    /**
     * cancel 응답 검증
     */
    private void validateCancelResponse(TossClientResponse.Cancel response) {
        if (response == null) {
            throw new IllegalStateException("PG 취소 응답이 null입니다.");
        }

        // 취소 응답은 status가 CANCELED여야 함
        if (response.status() != null && !"CANCELED".equals(response.status())) {
            throw new IllegalStateException(
                    "PG 취소가 완료 상태(CANCELED)가 아닙니다. status=" + response.status()
            );
        }
    }
}
