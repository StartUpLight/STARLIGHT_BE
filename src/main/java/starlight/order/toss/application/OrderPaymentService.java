package starlight.order.toss.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.order.toss.adapter.toss.TossClient;
import starlight.order.toss.adapter.webapi.dto.TossClientResponse;
import starlight.order.toss.adapter.webapi.dto.request.OrderCancelRequest;
import starlight.order.toss.application.provided.OrdersQuery;
import starlight.order.toss.domain.Money;
import starlight.order.toss.domain.OrderCode;
import starlight.order.toss.domain.Orders;
import starlight.order.toss.domain.PaymentRecords;
import starlight.order.toss.domain.exception.OrderErrorType;
import starlight.order.toss.domain.exception.OrderException;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderPaymentService {

    private final TossClient tossClient;
    private final OrdersQuery ordersQuery;

    /**
     * 결제 전 주문 준비
     * - 이미 해당 사업계획서에 PAID 주문이 있으면 예외
     * - orderCode로 주문이 있으면 재사용(검증 후 결제 시도 추가)
     * - 없으면 새 주문 생성 후 첫 결제 시도 추가
     *
     * @param orderCodeStr 프론트에서 생성한 주문번호
     * @param buyerId 구매자 ID
     * @param businessPlanId 사업계획서 ID
     * @param amount 결제 금액
     * @return Orders 준비된 주문
     */
    public Orders prepare(String orderCodeStr, Long buyerId, Long businessPlanId, Long amount) {
        if (ordersQuery.existsPaidByBuyerIdAndBusinessPlanId(buyerId, businessPlanId)) {
            throw new OrderException(OrderErrorType.ALREADY_PAID_FOR_BUSINESS_PLAN);
        }

        OrderCode orderCode = OrderCode.of(orderCodeStr);
        Money money = Money.krw(amount);

        return ordersQuery.findByOrderCode(orderCodeStr)
                .map(existing -> {
                    // 기존 주문 재사용: 같은 비즈니스 주문인지 검증
                    existing.validateSameBusinessOrder(buyerId, businessPlanId);

                    existing.addPaymentAttempt(money);

                    return existing;
                })
                .orElseGet(() -> {
                    // 신규 주문 생성
                    Orders newOrder = Orders.newOrder(orderCode, buyerId, businessPlanId, money);
                    newOrder.addPaymentAttempt(money);

                    return ordersQuery.save(newOrder);
                });
    }

    /**
     * 결제 승인 (Confirm)
     * 토스 리다이렉트 성공 후 호출
     *
     * @param orderCodeStr 주문번호
     * @param paymentKey 토스 결제키
     * @param amount 결제 금액
     * @return Orders 승인된 주문
     */
    public Orders confirm(String orderCodeStr, String paymentKey, Long amount) {

        Orders order = ordersQuery.getByOrderCodeOrThrow(orderCodeStr);

        PaymentRecords payment = order.getLatestRequestedOrThrow();

        TossClientResponse.Confirm response = tossClient.confirm(
                orderCodeStr, paymentKey, amount
        );

        String provider = response.providerOrNull();
        String receiptUrl = response.receiptUrlOrNull();
        Instant approvedAt = response.approvedAtOrNow();

        payment.markDone(
                response.paymentKey(), response.method(), provider, receiptUrl, approvedAt
        );

        order.markPaid();

        return ordersQuery.save(order);
    }

    /**
     * 결제 취소
     *
     * @param request 취소 요청
     * @return TossClientResponse.Cancel 취소 응답
     */
    public TossClientResponse.Cancel cancel(OrderCancelRequest request) {

        Orders order = ordersQuery.getByOrderCodeOrThrow(request.orderCode());

        PaymentRecords payment = order.getLatestDoneOrThrow();

        payment.ensureHasPaymentKey();

        TossClientResponse.Cancel response = tossClient.cancel(
                payment.getPaymentKey(), request.reason()
        );

        payment.markCanceled();
        order.cancel();

        ordersQuery.save(order);

        return response;
    }
}