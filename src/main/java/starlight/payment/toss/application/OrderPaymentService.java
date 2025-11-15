package starlight.payment.toss.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.payment.toss.adapter.persistence.OrdersRepository;
import starlight.payment.toss.adapter.toss.TossClient;
import starlight.payment.toss.adapter.webapi.dto.*;
import starlight.payment.toss.adapter.webapi.dto.request.OrderCancelRequest;
import starlight.payment.toss.application.provided.OrdersQuery;
import starlight.payment.toss.domain.Orders;
import starlight.payment.toss.domain.PaymentRecords;
import starlight.payment.toss.domain.enumerate.PaymentStatus;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderPaymentService {

    private final TossClient tossClient;
    private final OrdersQuery ordersQuery;
    private final OrdersRepository ordersRepository;

    /** 결제 전 준비(주문 생성/검증) */
    public Orders prepare(String orderCode, Long buyerId, Long businessPlanId, Long amount) {
        return ordersRepository.findByOrderCode(orderCode).map(existing -> {
            if (!Objects.equals(existing.getTotalAmount(), amount)) {
                throw new IllegalStateException("이미 존재하는 주문번호입니다. (금액 상이)");
            }
            if (existing.getStatus() == PaymentStatus.PAID) {
                throw new IllegalStateException("이미 결제가 완료된 주문입니다.");
            }
            if (existing.getPayment() == null) {
                PaymentRecords p = PaymentRecords.requestedFor(amount);
                existing.bindPayment(p);
            } else {
                existing.getPayment().markRequested(amount);
            }
            return existing;
        }).orElseGet(() -> {
            Orders order = Orders.newOrder(orderCode, buyerId, businessPlanId, amount);
            PaymentRecords payment = PaymentRecords.requestedFor(amount);
            order.bindPayment(payment);
            return ordersRepository.save(order);
        });
    }

    /** 리다이렉트 성공 후 승인(confirm) */
    public Orders confirm(String orderCode, String paymentKey, Long price) {

        Orders order = ordersQuery.findByOrderCode(orderCode);

        PaymentRecords paymentRecords = order.getPayment();
        // PG 승인 요청
        TossClientResponse.Confirm response = tossClient.confirm(orderCode, paymentKey, price);


        String provider = (response.easyPay() != null) ? response.easyPay().provider() : null;
        String receiptUrl = (response.receipt() != null) ? response.receipt().url() : null;
        Instant approvedAt = (response.approvedAt() != null) ? response.approvedAt().toInstant() : Instant.now();

        paymentRecords.markDone(
                response.paymentKey(), response.method(), provider, receiptUrl, approvedAt);
        order.markPaid();

        return ordersRepository.save(order);
    }

    public TossClientResponse.Cancel cancel(OrderCancelRequest req) {

        Orders order = ordersQuery.findByOrderCode(req.orderCode());

        PaymentRecords paymentRecords = order.getPayment();
        if (paymentRecords == null || paymentRecords.getPaymentKey() == null) {
            throw new IllegalStateException("paymentKey가 없어 PG 취소를 수행할 수 없습니다.");
        }

        // PG 취소 요청
        TossClientResponse.Cancel pgResponse = tossClient.cancel(paymentRecords.getPaymentKey(), req.reason());

        // 상태 업데이트
        paymentRecords.markCanceled();
        order.cancel();
        ordersRepository.save(order);

        return pgResponse;
    }

}

