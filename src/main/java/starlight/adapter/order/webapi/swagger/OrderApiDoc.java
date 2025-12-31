package starlight.adapter.order.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import starlight.shared.auth.AuthenticatedMember;
import starlight.adapter.order.webapi.dto.request.OrderCancelRequest;
import starlight.adapter.order.webapi.dto.request.OrderConfirmRequest;
import starlight.adapter.order.webapi.dto.request.OrderPrepareRequest;
import starlight.adapter.order.webapi.dto.response.OrderCancelResponse;
import starlight.adapter.order.webapi.dto.response.OrderConfirmResponse;
import starlight.adapter.order.webapi.dto.response.OrderPrepareResponse;
import starlight.application.order.provided.dto.PaymentHistoryItemResult;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Tag(name = "결제", description = "결제 관련 API")
public interface OrderApiDoc {

    @Operation(summary = "결제 준비", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderPrepareResponse.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "SUCCESS",
                          "data": {
                            "orderCode": "O-20250101-0001",
                            "amount": 150000,
                            "productCode": "USAGE_10"
                          },
                          "error": null
                        }
                        """
                            )
                    )
            )
            ,
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "유효하지 않은 이용권",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_USAGE_PRODUCT",
                                "message": "유효하지 않은 이용권 금액입니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "주문번호-구매자 불일치",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "ORDER_CODE_BUYER_MISMATCH",
                                "message": "이미 존재하는 주문번호입니다. (구매자 상이)"
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "주문번호-이용권 불일치",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "ORDER_PRODUCT_MISMATCH",
                                "message": "이미 존재하는 주문번호입니다. (이용권 금액 상이)"
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "이미 결제된 주문",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "ALREADY_PAID",
                                "message": "이미 결제가 완료된 주문입니다."
                              }
                            }
                            """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/request")
    ApiResponse<OrderPrepareResponse> prepareOrder(
            @Valid @RequestBody OrderPrepareRequest request,
            @AuthenticationPrincipal AuthenticatedMember authDetails
    );

    @Operation(summary = "결제 승인", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderConfirmResponse.class)
                    )
            )
            ,
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "결제 금액 불일치",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "PAYMENT_AMOUNT_MISMATCH",
                                "message": "주문 금액과 결제 금액이 일치하지 않습니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "승인 가능한 결제 없음",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "NO_REQUESTED_PAYMENT",
                                "message": "승인 가능한 결제 시도가 존재하지 않습니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "결제 상태 오류",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_ORDER_STATE_FOR_PAYMENT",
                                "message": "주문 생성 상태에서만 결제 가능합니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "PG 승인 실패",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "TOSS_CLIENT_CONFIRM_ERROR",
                                "message": "토스 결제 요청 중 오류가 발생했습니다."
                              }
                            }
                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "ORDER_NOT_FOUND",
                            "message": "주문을 찾을 수 없습니다."
                          }
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/confirm")
    ApiResponse<OrderConfirmResponse> confirmPayment(
            @Valid @RequestBody OrderConfirmRequest request,
            @AuthenticationPrincipal AuthenticatedMember authDetails
    );

    @Operation(summary = "결제 취소")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderCancelResponse.class)
                    )
            )
            ,
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "취소 가능한 결제 이력 없음",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "NO_PAYMENT_RECORDS",
                                "message": "주문에 결제 이력이 존재하지 않습니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "paymentKey 누락",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "NO_PAYMENT_KEY",
                                "message": "paymentKey가 없어 PG 취소를 수행할 수 없습니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "결제 상태 오류",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_ORDER_STATE_FOR_CANCEL",
                                "message": "결제 완료 상태에서만 취소 가능합니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "PG 취소 실패",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "TOSS_CLIENT_CANCEL_ERROR",
                                "message": "토스 결제 취소 요청 중 오류가 발생했습니다."
                              }
                            }
                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "ORDER_NOT_FOUND",
                            "message": "주문을 찾을 수 없습니다."
                          }
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/cancel")
    ApiResponse<OrderCancelResponse> cancelPayment(
            @Valid @RequestBody OrderCancelRequest request
    );

    @Operation(summary = "내 결제 내역 조회", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PaymentHistoryItemResult.class))
                    )
            )
    })
    @GetMapping
    ApiResponse<List<PaymentHistoryItemResult>> getMyPayments(
            @AuthenticationPrincipal AuthenticatedMember authDetails
    );
}
