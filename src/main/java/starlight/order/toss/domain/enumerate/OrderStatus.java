package starlight.order.toss.domain.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    NEW("주문 생성됨 (결제 전)"),
    PAID("결제 완료"),
    CANCELED("주문/결제 취소");

    private final String description;
}
