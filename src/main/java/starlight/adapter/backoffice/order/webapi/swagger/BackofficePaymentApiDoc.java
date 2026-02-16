package starlight.adapter.backoffice.order.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.adapter.backoffice.order.webapi.dto.response.BackofficePaymentPageResponse;
import starlight.domain.order.enumerate.OrderStatus;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "[Office] 결제", description = "백오피스 결제 조회 API")
public interface BackofficePaymentApiDoc {

    @Operation(
            summary = "결제 목록 조회(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(
                            value = """
                                    {
                                      "result": "SUCCESS",
                                      "data": {
                                        "content": [
                                          {
                                            "orderId": 1,
                                            "orderCode": "ORD-20260216-001",
                                            "userId": 10,
                                            "userName": "홍길동",
                                            "userEmail": "test@example.com",
                                            "usageProductCode": "AI_REPORT_1",
                                            "usageCount": 1,
                                            "price": 9900,
                                            "orderStatus": "PAID",
                                            "latestPaymentStatus": "DONE",
                                            "createdAt": "2026-02-16T00:00:00",
                                            "approvedAt": "2026-02-16T00:03:12Z",
                                            "paymentKey": "pay_abc123"
                                          }
                                        ],
                                        "page": 0,
                                        "size": 20,
                                        "totalElements": 1,
                                        "totalPages": 1,
                                        "hasNext": false
                                      },
                                      "error": null
                                    }
                                    """
                    ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                                    {
                                      "result": "ERROR",
                                      "data": null,
                                      "error": {
                                        "code": "INVALID_REQUEST_ARGUMENT",
                                        "message": "잘못된 요청 인자입니다."
                                      }
                                    }
                                    """
                    ))
            )
    })
    @GetMapping("/v1/backoffice/payments")
    ApiResponse<BackofficePaymentPageResponse> findPayments(
            @Parameter(description = "주문 상태 필터 (NEW | PAID | CANCELED)") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "검색 키워드 (orderCode/userName/userEmail/usageProductCode)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "정렬 (예: createdAt,desc)") @RequestParam(required = false, defaultValue = "createdAt,desc") String sort
    );
}
