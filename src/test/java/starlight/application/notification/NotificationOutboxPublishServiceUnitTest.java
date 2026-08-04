package starlight.application.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import starlight.application.notification.required.NotificationOutboxCommandPort;
import starlight.application.notification.required.NotificationOutboxQueryPort;
import starlight.application.notification.required.NotificationPublishPort;
import starlight.application.notification.required.NotificationQueryPort;
import starlight.application.notification.required.dto.NotificationPublishMessage;
import starlight.domain.notification.entity.Notification;
import starlight.domain.notification.entity.NotificationOutbox;
import starlight.domain.notification.enumerate.NotificationOutboxStatus;
import starlight.domain.notification.exception.NotificationErrorType;
import starlight.domain.notification.exception.NotificationException;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxPublishServiceUnitTest {

    @Mock
    private NotificationOutboxQueryPort notificationOutboxQueryPort;

    @Mock
    private NotificationOutboxCommandPort notificationOutboxCommandPort;

    @Mock
    private NotificationQueryPort notificationQueryPort;

    @Mock
    private NotificationPublishPort notificationPublishPort;

    @Mock
    private TransactionTemplate transactionTemplate;

    private NotificationOutboxPublishService notificationOutboxPublishService;

    @BeforeEach
    void setUp() {
        notificationOutboxPublishService = new NotificationOutboxPublishService(
                notificationOutboxQueryPort,
                notificationOutboxCommandPort,
                notificationQueryPort,
                notificationPublishPort,
                transactionTemplate
        );
        ReflectionTestUtils.setField(notificationOutboxPublishService, "maxRetryCount", 5);
        ReflectionTestUtils.setField(notificationOutboxPublishService, "retryDelaySeconds", 10L);
        ReflectionTestUtils.setField(notificationOutboxPublishService, "processingTimeoutSeconds", 60L);

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        doAnswer(invocation -> {
            Consumer<?> callback = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Consumer<Object> transactionCallback = (Consumer<Object>) callback;
            transactionCallback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        when(notificationOutboxCommandPort.save(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void publish_성공하면_outbox를_SENT로_변경한다() {
        NotificationOutbox outbox = outbox(20L, 10L);
        Notification notification = notification(10L);

        when(notificationOutboxQueryPort.findByIdOrThrow(20L)).thenReturn(outbox);
        when(notificationQueryPort.findByIdOrThrow(10L)).thenReturn(notification);

        boolean result = notificationOutboxPublishService.publish(20L);

        assertTrue(result);
        assertEquals(NotificationOutboxStatus.SENT, outbox.getStatus());
        verify(notificationPublishPort).publish(any(NotificationPublishMessage.class));
    }

    @Test
    void publish_발행실패하면_재시도대상으로_변경한다() {
        NotificationOutbox outbox = outbox(20L, 10L);
        Notification notification = notification(10L);

        when(notificationOutboxQueryPort.findByIdOrThrow(20L)).thenReturn(outbox);
        when(notificationQueryPort.findByIdOrThrow(10L)).thenReturn(notification);
        doThrow(new RedisConnectionFailureException("redis down"))
                .when(notificationPublishPort).publish(any(NotificationPublishMessage.class));

        assertThrows(RedisConnectionFailureException.class, () -> notificationOutboxPublishService.publish(20L));

        assertEquals(NotificationOutboxStatus.FAILED, outbox.getStatus());
        assertEquals(1, outbox.getRetryCount());
        assertNotNull(outbox.getNextRetryAt());
    }

    @Test
    void publish_알림이없으면_영구실패로_처리한다() {
        NotificationOutbox outbox = outbox(20L, 10L);

        when(notificationOutboxQueryPort.findByIdOrThrow(20L)).thenReturn(outbox);
        when(notificationQueryPort.findByIdOrThrow(10L))
                .thenThrow(new NotificationException(NotificationErrorType.NOTIFICATION_NOT_FOUND));

        boolean result = notificationOutboxPublishService.publish(20L);

        assertFalse(result);
        assertEquals(NotificationOutboxStatus.DISCARDED, outbox.getStatus());
        assertEquals(0, outbox.getRetryCount());
        verifyNoInteractions(notificationPublishPort);
    }

    private NotificationOutbox outbox(Long outboxId, Long notificationId) {
        NotificationOutbox outbox = NotificationOutbox.create(notificationId, LocalDateTime.now().minusSeconds(1));
        ReflectionTestUtils.setField(outbox, "id", outboxId);
        return outbox;
    }

    private Notification notification(Long notificationId) {
        Notification notification = Notification.create(1L, "SYSTEM", "title", "message", 99L);
        ReflectionTestUtils.setField(notification, "id", notificationId);
        return notification;
    }
}
