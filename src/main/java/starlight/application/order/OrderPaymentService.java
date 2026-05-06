package starlight.application.order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.application.order.provided.dto.PaymentHistoryItemResult;
import starlight.application.order.provided.OrderPaymentServiceUseCase;
import starlight.application.order.provided.dto.TossClientResult;
import starlight.application.order.required.OrderCommandPort;
import starlight.application.order.required.OrderQueryPort;
import starlight.application.order.required.PaymentGatewayPort;
import starlight.application.order.required.UsageCreditChargePort;
import starlight.domain.order.enumerate.OrderStatus;
import starlight.domain.order.enumerate.UsageProductType;
import starlight.domain.order.exception.OrderErrorType;
import starlight.domain.order.exception.OrderException;
import starlight.domain.order.order.Orders;
import starlight.domain.order.order.PaymentRecords;
import starlight.domain.order.order.vo.Money;
import starlight.domain.order.order.vo.OrderCode;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderPaymentService implements OrderPaymentServiceUseCase {

    private final PaymentGatewayPort paymentGatewayPort;
    private final OrderQueryPort orderQueryPort;
    private final OrderCommandPort orderCommandPort;
    private final UsageCreditChargePort usageCreditChargePort;

    /**
     * 결제 전 주문 준비
     * - orderCode로 주문이 있으면 재사용(검증 후 결제 시도 추가)
     * - 없으면 새 주문 생성 후 첫 결제 시도 추가
     *
     * @param orderCodeStr 프론트에서 생성한 주문번호
     * @param buyerId 구매자 ID
     * @param productCode 결제 금액
     * @return Orders 준비된 주문
     */
    @Override
    public Orders prepare(String orderCodeStr, Long buyerId, String productCode) {
        UsageProductType product = UsageProductType.fromCode(productCode);
        Money money = Money.krw(product.getPrice());
        OrderCode orderCode = OrderCode.of(orderCodeStr);

        return orderQueryPort.findByOrderCode(orderCodeStr)
                .map(existing -> {
                    existing.validateSameBuyer(buyerId);
                    existing.validateSameProduct(product);
                    existing.addPaymentAttempt(money);
                    return existing;
                })
                .orElseGet(() -> {
                    Orders newOrder = Orders.newUsageOrder(orderCode, buyerId, money, product);
                    newOrder.addPaymentAttempt(money);
                    return orderCommandPort.save(newOrder);
                });
    }

    /**
     * 결제 승인 (Confirm)
     * 토스 리다이렉트 성공 후 호출
     *
     * @param orderCodeStr 주문번호
     * @param paymentKey 토스 결제키
     * @return Orders 승인된 주문
     */
    @Override
    public Orders confirm(String orderCodeStr, String paymentKey, Long buyerId) {

        Orders order = orderQueryPort.getByOrderCodeOrThrow(orderCodeStr);

        UsageProductType product = UsageProductType.fromCode(order.getUsageProductCode());
        long expectedAmount = product.getPrice();

        if (!Objects.equals(order.getPrice(), expectedAmount)) {
            throw new OrderException(OrderErrorType.PAYMENT_AMOUNT_MISMATCH);
        }

        PaymentRecords payment = order.getLatestRequestedOrThrow();

        TossClientResult.Confirm response = paymentGatewayPort.confirm(
                orderCodeStr, paymentKey, expectedAmount
        );

        String provider = response.providerOrNull();
        String receiptUrl = response.receiptUrlOrNull();
        Instant approvedAt = response.approvedAtOrNow();

        payment.markDone(
                response.paymentKey(), response.method(), provider, receiptUrl, approvedAt
        );
        order.markPaid();

        usageCreditChargePort.chargeForOrder(
                order.getBuyerId(),
                order.getId(),
                product.getUsageCount()
        );

        return orderCommandPort.save(order);
    }

    /**
     * 결제 취소
     *
     * @param orderCode 주문번호
     * @param reason 취소 사유
     * @return TossClientResult.Cancel 취소 응답
     */
    @Override
    public TossClientResult.Cancel cancel(String orderCode, String reason) {

        Orders order = orderQueryPort.getByOrderCodeOrThrow(orderCode);

        PaymentRecords payment = order.getLatestDoneOrThrow();
        payment.ensureHasPaymentKey();

        TossClientResult.Cancel response = paymentGatewayPort.cancel(
                payment.getPaymentKey(), reason
        );

        payment.markCanceled();
        order.cancel();

        orderCommandPort.save(order);

        return response;
    }

    public List<PaymentHistoryItemResult> getPaymentHistory(Long buyerId) {
        // 1) 이 회원(buyer)의 주문 전체를 최신순으로 가져오기
        List<Orders> orders = orderQueryPort.findAllWithPaymentsByBuyerIdOrderByCreatedAtDesc(buyerId);

        return orders.stream()
                // 결제완료(PAID) 주문만
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .map(order -> {
                    // 2) 해당 주문의 가장 최근 DONE 결제
                    PaymentRecords payment = order.getLatestDoneOrThrow();

                    // 3) 상품명 매핑
                    UsageProductType productType = UsageProductType.fromCode(order.getUsageProductCode());
                    String productName = productType.getDescription();

                    // 4) 결제일 (approvedAt 없으면 createdAt)
                    Instant paidAt = payment.getApprovedAt() != null
                            ? payment.getApprovedAt()
                            : payment.getCreatedAt();

                    return PaymentHistoryItemResult.of(
                            productName,
                            payment.getMethod(),
                            payment.getPrice(),
                            paidAt,
                            payment.getReceiptUrl()
                    );
                })
                .toList();
    }
}
