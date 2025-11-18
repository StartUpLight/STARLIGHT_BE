package starlight.order.toss.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum OrderErrorType implements ErrorType {

    // PG 통신 에러
    TOSS_CLIENT_CONFIRM_ERROR(HttpStatus.BAD_REQUEST, "토스 결제 요청 중 오류가 발생했습니다."),
    TOSS_CLIENT_CANCEL_ERROR(HttpStatus.BAD_REQUEST, "토스 결제 취소 요청 중 오류가 발생했습니다."),

    // 도메인 규칙 위반 - 결제 상태
    ALREADY_PAID(HttpStatus.BAD_REQUEST, "이미 결제가 완료된 주문입니다."),
    INVALID_ORDER_STATE_FOR_PAYMENT(HttpStatus.BAD_REQUEST, "주문 생성 상태에서만 결제 가능합니다."),
    INVALID_ORDER_STATE_FOR_CANCEL(HttpStatus.BAD_REQUEST, "결제 완료 상태에서만 취소 가능합니다."),

    // 도메인 규칙 위반 - 금액/주문번호
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "주문 금액과 결제 금액이 일치하지 않습니다."),
    ORDER_CODE_BUYER_MISMATCH(HttpStatus.BAD_REQUEST, "이미 존재하는 주문번호입니다. (구매자 상이)"),
    ORDER_CODE_BUSINESS_PLAN_MISMATCH(HttpStatus.BAD_REQUEST, "이미 존재하는 주문번호입니다. (사업계획서 상이)"),

    // 결제 이력 없음
    NO_REQUESTED_PAYMENT(HttpStatus.BAD_REQUEST, "승인 가능한 결제 시도가 존재하지 않습니다."),
    NO_DONE_PAYMENT(HttpStatus.BAD_REQUEST, "취소 가능한 결제 이력이 존재하지 않습니다."),
    NO_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "paymentKey가 없어 PG 취소를 수행할 수 없습니다."),

    // 조회 실패
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ALREADY_PAID_FOR_BUSINESS_PLAN(HttpStatus.BAD_REQUEST, "이미 결제가 완료된 사업계획서입니다.")
    ;

    private final HttpStatus status;

    private final String message;
}
