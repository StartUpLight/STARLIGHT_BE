package starlight.order.toss.domain.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import starlight.order.toss.domain.exception.OrderErrorType;
import starlight.order.toss.domain.exception.OrderException;

@Getter
@RequiredArgsConstructor
public enum UsageProductType {

    AI_REPORT_1("AI_REPORT_1", 1, 69_000L, "AI 리포트 1회권"),
    AI_REPORT_2("AI_REPORT_2", 2, 99_000L, "AI 리포트 2회권");

    private final String code;        // 상품 코드
    private final int usageCount;     // 사용 가능 횟수
    private final long price;         // 가격
    private final String description; // 설명

    public static UsageProductType fromCode(String code) {
        return switch (code) {
            case "AI_REPORT_1" -> AI_REPORT_1;
            case "AI_REPORT_2" -> AI_REPORT_2;
            default -> throw new OrderException(OrderErrorType.INVALID_USAGE_PRODUCT);
        };
    }
}
