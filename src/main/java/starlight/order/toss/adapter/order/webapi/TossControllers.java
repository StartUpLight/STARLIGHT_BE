package starlight.order.toss.adapter.order.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import starlight.order.toss.adapter.order.webapi.dto.TossClientResponse;
import starlight.order.toss.adapter.order.webapi.dto.request.OrderCancelRequest;
import starlight.order.toss.adapter.order.webapi.dto.request.OrderConfirmRequest;
import starlight.order.toss.adapter.order.webapi.dto.request.OrderPrepareRequest;
import starlight.order.toss.adapter.order.webapi.dto.response.OrderCancelResponse;
import starlight.order.toss.adapter.order.webapi.dto.response.OrderConfirmResponse;
import starlight.order.toss.adapter.order.webapi.dto.response.OrderPrepareResponse;
import starlight.order.toss.application.order.OrderPaymentService;
import starlight.order.toss.domain.order.Orders;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequiredArgsConstructor
public class TossControllers {

    private final OrderPaymentService orderPaymentService;

    /**
     * 결제 준비 (주문 생성)
     * POST /api/toss/request
     */
    @PostMapping("/api/toss/request")
    public ApiResponse<OrderPrepareResponse> prepareOrder(
            @Valid @RequestBody OrderPrepareRequest request
    ) {
        Orders order = orderPaymentService.prepare(
                request.orderCode(),
                request.buyerId(),
                request.businessPlanId(),
                request.productCode()
        );

        OrderPrepareResponse response = OrderPrepareResponse.from(order);

        return ApiResponse.success(response);
    }

    /**
     * 결제 승인
     * POST /api/toss/confirm
     */
    @PostMapping("/api/toss/confirm")
    public ApiResponse<OrderConfirmResponse> confirmPayment(
            @Valid @RequestBody OrderConfirmRequest request
    ) {
        Orders order = orderPaymentService.confirm(
                request.orderCode(),
                request.paymentKey()
        );

        OrderConfirmResponse response = OrderConfirmResponse.from(order);

        return ApiResponse.success(response);
    }

    /**
     * 결제 취소
     * POST /api/toss/cancel
     */
    @PostMapping("/api/toss/cancel")
    public ApiResponse<OrderCancelResponse> cancelPayment(
            @Valid @RequestBody OrderCancelRequest request
    ) {
        TossClientResponse.Cancel tossResponse = orderPaymentService.cancel(request);

        OrderCancelResponse response = OrderCancelResponse.from(tossResponse);

        return ApiResponse.success(response);
    }
}