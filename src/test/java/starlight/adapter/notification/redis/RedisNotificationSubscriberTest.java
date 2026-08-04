package starlight.adapter.notification.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import starlight.application.notification.required.NotificationRealtimePort;
import starlight.application.notification.required.dto.NotificationPublishMessage;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisNotificationSubscriberTest {

    @Mock
    private NotificationRealtimePort notificationRealtimePort;

    private RedisNotificationSubscriber redisNotificationSubscriber;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        redisNotificationSubscriber = new RedisNotificationSubscriber(objectMapper, notificationRealtimePort);
    }

    @Test
    void onMessage_정상메시지면_SSE전달을_호출한다() throws Exception {
        NotificationPublishMessage publishMessage = new NotificationPublishMessage(
                1L,
                2L,
                "SYSTEM",
                "title",
                "message",
                3L,
                LocalDateTime.of(2026, 3, 27, 12, 0)
        );

        String payload = objectMapper.writeValueAsString(publishMessage);

        redisNotificationSubscriber.handlePayload(payload);

        verify(notificationRealtimePort).send(publishMessage);
    }

    @Test
    void onMessage_잘못된메시지면_무시한다() {
        redisNotificationSubscriber.handlePayload("not-json");

        verifyNoInteractions(notificationRealtimePort);
    }
}
