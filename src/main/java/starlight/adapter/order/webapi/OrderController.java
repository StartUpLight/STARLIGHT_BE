package starlight.adapter.order.webapi;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.application.order.provided.dto.TossClientResponse;
import starlight.adapter.order.webapi.dto.request.OrderCancelRequest;
import starlight.adapter.order.webapi.dto.request.OrderConfirmRequest;
import starlight.adapter.order.webapi.dto.request.OrderPrepareRequest;
import starlight.adapter.order.webapi.dto.response.OrderCancelResponse;
import starlight.adapter.order.webapi.dto.response.OrderConfirmResponse;
import starlight.adapter.order.webapi.dto.response.OrderPrepareResponse;
import starlight.application.order.provided.OrderPaymentService;
import starlight.application.order.provided.dto.PaymentHistoryItemDto;
import starlight.application.usage.provided.UsageCreditPort;
import starlight.domain.order.order.Orders;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "결제", description = "결제 관련 API")
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderPaymentService orderPaymentService;
    private final UsageCreditPort usageCreditPort;

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
        TossClientResponse.Cancel tossResponse = orderPaymentService.cancel(request);

        OrderCancelResponse response = OrderCancelResponse.from(tossResponse);

        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<PaymentHistoryItemDto>> getMyPayments(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        Long memberId = authDetails.getMemberId();
        List<PaymentHistoryItemDto> history = orderPaymentService.getPaymentHistory(memberId);

        return ApiResponse.success(history);
    }
}
