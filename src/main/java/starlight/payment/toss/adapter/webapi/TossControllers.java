package starlight.payment.toss.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import starlight.payment.toss.adapter.webapi.dto.request.OrderCancelRequest;
import starlight.payment.toss.adapter.webapi.dto.request.OrderConfirmRequest;
import starlight.payment.toss.adapter.webapi.dto.request.OrderPrepareRequest;
import starlight.payment.toss.adapter.webapi.dto.TossClientResponse;
import starlight.payment.toss.application.OrderPaymentService;
import starlight.payment.toss.domain.Orders;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TossControllers {

    private final OrderPaymentService service;

    @PostMapping("/api/toss/request")
    public ApiResponse<?> request(@Valid @RequestBody OrderPrepareRequest request) {

        Orders orders = service.prepare(
                request.orderCode(), request.buyerId(), request.businessPlanId(), request.price());

        return ApiResponse.success(Map.of(
                "orderCode", orders.getOrderCode(),
                "amount",  orders.getTotalAmount(),
                "status",  orders.getStatus()
        ));
    }

    @PostMapping("/api/toss/confirm")
    public ApiResponse<?> confirm(@Valid @RequestBody OrderConfirmRequest request) {

        Orders order = service.confirm(request.orderCode(), request.paymentKey(), request.price());

        return ApiResponse.success(Map.of(
                "businessPlanId", order.getBusinessPlanId(),
                "buyerId",       order.getBuyerId(),
                "paymentKey",   order.getPayment().getPaymentKey(),
                "orderId",    order.getOrderCode(),
                "amount",     order.getTotalAmount(),
                "status",     order.getStatus().name(),
                "approvedAt", order.getPayment().getApprovedAt(),
                "receiptUrl", order.getPayment().getReceiptUrl()
        ));
    }

    @PostMapping("/api/toss/cancel")
    public ApiResponse<?> cancel(@Valid @RequestBody OrderCancelRequest request) {
        TossClientResponse.Cancel cancelResponse = service.cancel(request);
        return ApiResponse.success(cancelResponse);
    }
}
