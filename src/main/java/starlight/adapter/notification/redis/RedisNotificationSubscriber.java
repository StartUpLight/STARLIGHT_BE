package starlight.adapter.notification.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import starlight.application.notification.required.NotificationRealtimePort;
import starlight.application.notification.required.dto.NotificationPublishMessage;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final NotificationRealtimePort notificationRealtimePort;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        handlePayload(payload);
    }

    void handlePayload(String payload) {
        try {
            NotificationPublishMessage publishMessage = objectMapper.readValue(payload, NotificationPublishMessage.class);
            notificationRealtimePort.send(publishMessage);
        } catch (Exception exception) {
            log.warn("[NOTIFICATION] failed to consume redis payload={}", payload, exception);
        }
    }
}
