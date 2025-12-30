package starlight.application.order.required;

public interface UsageCreditChargePort {

    void chargeForOrder(Long userId, Long orderId, int usageCount);
}
