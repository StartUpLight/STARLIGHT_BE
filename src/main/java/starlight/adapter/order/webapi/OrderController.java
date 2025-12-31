package starlight.adapter.order.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.order.webapi.swagger.OrderApiDoc;
import starlight.application.order.provided.dto.TossClientResult;
import starlight.adapter.order.webapi.dto.request.OrderCancelRequest;
import starlight.adapter.order.webapi.dto.request.OrderConfirmRequest;
import starlight.adapter.order.webapi.dto.request.OrderPrepareRequest;
import starlight.adapter.order.webapi.dto.response.OrderCancelResponse;
import starlight.adapter.order.webapi.dto.response.OrderConfirmResponse;
import starlight.adapter.order.webapi.dto.response.OrderPrepareResponse;
import starlight.application.order.provided.OrderPaymentServiceUseCase;
import starlight.application.order.provided.dto.PaymentHistoryItemResult;
import starlight.domain.order.order.Orders;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController implements OrderApiDoc {

    private final OrderPaymentServiceUseCase orderPaymentService;

    @PostMapping("/request")
    public ApiResponse<OrderPrepareResponse> prepareOrder(
            @Valid @RequestBody OrderPrepareRequest request,
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        Orders order = orderPaymentService.prepare(
                request.orderCode(),
                authDetails.getMemberId(),
                request.productCode()
        );

        OrderPrepareResponse response = OrderPrepareResponse.from(order);

        return ApiResponse.success(response);
    }

    @PostMapping("/confirm")
    public ApiResponse<OrderConfirmResponse> confirmPayment(
            @Valid @RequestBody OrderConfirmRequest request,
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        Orders order = orderPaymentService.confirm(
                request.orderCode(),
                request.paymentKey(),
                authDetails.getMemberId()
        );

        OrderConfirmResponse response = OrderConfirmResponse.from(order);

        return ApiResponse.success(response);
    }

    @PostMapping("/cancel")
    public ApiResponse<OrderCancelResponse> cancelPayment(
            @Valid @RequestBody OrderCancelRequest request
    ) {
        TossClientResult.Cancel tossResponse = orderPaymentService.cancel(
                request.orderCode(),
                request.reason()
        );

        OrderCancelResponse response = OrderCancelResponse.from(tossResponse);

        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<PaymentHistoryItemResult>> getMyPayments(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        Long memberId = authDetails.getMemberId();
        List<PaymentHistoryItemResult> history = orderPaymentService.getPaymentHistory(memberId);

        return ApiResponse.success(history);
    }
}
