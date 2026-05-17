package starlight.adapter.notification.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import starlight.application.notification.provided.NotificationUseCase;
import starlight.application.notification.required.dto.NotificationPublishMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisNotificationSubscriberTest {

    @Mock
    private NotificationUseCase notificationUseCase;

    private RedisNotificationSubscriber redisNotificationSubscriber;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        redisNotificationSubscriber = new RedisNotificationSubscriber(objectMapper, notificationUseCase);
    }

    @Test
    void onMessage_정상메시지면_SSE전달을_호출한다() throws Exception {
        NotificationPublishMessage publishMessage = new NotificationPublishMessage(
                1L,
                2L,
                "SYSTEM"
        );

        String payload = objectMapper.writeValueAsString(publishMessage);

        redisNotificationSubscriber.handlePayload(payload);

        verify(notificationUseCase).sendRealtime(publishMessage);
    }

    @Test
    void onMessage_잘못된메시지면_무시한다() {
        redisNotificationSubscriber.handlePayload("not-json");

        verifyNoInteractions(notificationUseCase);
    }
}
