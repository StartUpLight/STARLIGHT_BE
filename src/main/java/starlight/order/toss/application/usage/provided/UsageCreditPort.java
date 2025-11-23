package starlight.order.toss.application.usage.provided;

import starlight.order.toss.domain.order.vo.Money;

public interface UsageCreditPort {

    /**
     * 주문 결제가 완료되었을 때 사용권(지갑)을 충전한다.
     *
     * @param userId     주문자 ID
     * @param orderId    주문 PK (UsageHistory 연동용)
     * @param usageCount 몇 회권인지 (1회 / 2회 등)
     */
    void chargeForOrder(Long userId, Long orderId, int usageCount);
}
