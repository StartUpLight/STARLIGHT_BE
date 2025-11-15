package starlight.payment.toss.adapter.toss;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import starlight.payment.toss.adapter.webapi.dto.TossClientResponse;
import starlight.payment.toss.domain.exception.OrderErrorType;
import starlight.payment.toss.domain.exception.OrderException;

import java.util.Map;

@Slf4j
@Component
public class TossClient {

    private final RestClient restClient;

    public TossClient(@Qualifier("tossRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public TossClientResponse.Confirm confirm(String orderCode, String paymentKey, Long price) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId",    orderCode,
                "amount",     price
        );
        try {
            return restClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", orderCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TossClientResponse.Confirm.class);

        } catch (Exception e) {
            log.error("토스 환불 요청 중 에러발생: {}", e.getMessage(), e);
            log.error("토스 결제 확인 요청 중 에러 발생. orderId: {}, paymentKey: {}, message: {}", orderCode, paymentKey, e.getMessage(), e);
            throw new OrderException(OrderErrorType.TOSS_CLIENT_CONFIRM_ERROR);
        }
    }

    public TossClientResponse.Cancel cancel(String paymentKey, String reason) {
        Map<String, Object> body = Map.of(
                "cancelReason", reason != null ? reason : "user_request"
        );
        try {
            return restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TossClientResponse.Cancel.class);

        } catch (Exception e) {
            log.error("토스 환불 요청 중 에러발생: {}", e.getMessage(), e);
            log.error("paymentKey: {}, reason: {}", paymentKey, reason);
            throw new OrderException(OrderErrorType.TOSS_CLIENT_CONFIRM_ERROR);
        }

    }
}
