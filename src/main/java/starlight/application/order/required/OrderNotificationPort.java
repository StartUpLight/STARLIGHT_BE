package starlight.application.order.required;

public interface OrderNotificationPort {

    void sendPaymentCompleted(Long memberId, Long orderId, String productName, int usageCount);
}
