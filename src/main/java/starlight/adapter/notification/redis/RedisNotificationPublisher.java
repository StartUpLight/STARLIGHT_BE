package starlight.adapter.notification.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import starlight.application.notification.required.NotificationPublishPort;
import starlight.application.notification.required.dto.NotificationPublishMessage;

@Component
@RequiredArgsConstructor
public class RedisNotificationPublisher implements NotificationPublishPort {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.redis.topic:notification:events}")
    private String notificationTopic;

    @Override
    public void publish(NotificationPublishMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            stringRedisTemplate.convertAndSend(notificationTopic, payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize notification payload", exception);
        }
    }
}
