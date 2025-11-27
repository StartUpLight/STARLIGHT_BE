package starlight.application.usage.provided;

public interface UsageCreditPort {

    void chargeForOrder(Long userId, Long orderId, int usageCount);
}
