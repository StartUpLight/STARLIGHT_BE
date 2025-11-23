package starlight.order.toss.domain.enumerate;

import starlight.order.toss.domain.exception.OrderErrorType;
import starlight.order.toss.domain.exception.OrderException;

public enum UsageProductType {

    AI_REPORT_1("AI_REPORT_1", 1, 69_000L, "AI 리포트 1회권"),
    AI_REPORT_2("AI_REPORT_2", 2, 99_000L, "AI 리포트 2회권");

    private final String code;        // 요청/DB에 저장할 상품 코드
    private final int usageCount;     // 몇 회권인지
    private final long price;         // 정가
    private final String displayName; // 설명용 이름

    UsageProductType(String code, int usageCount, long price, String displayName) {
        this.code = code;
        this.usageCount = usageCount;
        this.price = price;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public long getPrice() {
        return price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UsageProductType fromCode(String code) {
        for (UsageProductType t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new OrderException(OrderErrorType.INVALID_USAGE_PRODUCT);
    }
}
