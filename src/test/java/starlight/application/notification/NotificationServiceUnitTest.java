package starlight.application.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.event.NotificationCreatedEvent;
import starlight.application.notification.provided.dto.input.NotificationSendInput;
import starlight.application.notification.provided.dto.result.NotificationResult;
import starlight.application.notification.required.NotificationCommandPort;
import starlight.application.notification.required.NotificationQueryPort;
import starlight.application.notification.required.NotificationRealtimePort;
import starlight.domain.notification.entity.Notification;
import starlight.domain.notification.exception.NotificationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @Mock
    private NotificationCommandPort notificationCommandPort;

    @Mock
    private NotificationQueryPort notificationQueryPort;

    @Mock
    private NotificationRealtimePort notificationRealtimePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notifyMember_저장후_이벤트를_발행한다() {
        NotificationSendInput input = NotificationSendInput.of(
                1L, "SYSTEM", "title", "message", 99L
        );
        Notification savedNotification = Notification.create(1L, "SYSTEM", "title", "message", 99L);

        when(notificationCommandPort.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.notifyMember(input);

        verify(notificationCommandPort).save(any(Notification.class));
        verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    void findAllByMemberId_알림목록을_반환한다() {
        Notification notification = Notification.create(1L, "SYSTEM", "title", "message", null);

        when(notificationQueryPort.findAllByMemberIdOrderByIdDesc(1L)).thenReturn(List.of(notification));

        List<NotificationResult> result = notificationService.findAllByMemberId(1L);

        assertEquals(1, result.size());
        assertEquals("SYSTEM", result.getFirst().type());
    }

    @Test
    void markAsRead_소유자면_읽음처리한다() {
        Notification notification = Notification.create(1L, "SYSTEM", "title", "message", null);

        when(notificationQueryPort.findByIdOrThrow(10L)).thenReturn(notification);

        notificationService.markAsRead(10L, 1L);

        assertTrue(notification.isRead());
        verify(notificationCommandPort).save(notification);
    }

    @Test
    void markAsRead_타인이면_예외가_발생한다() {
        Notification notification = Notification.create(1L, "SYSTEM", "title", "message", null);

        when(notificationQueryPort.findByIdOrThrow(10L)).thenReturn(notification);

        assertThrows(NotificationException.class, () -> notificationService.markAsRead(10L, 2L));
        verify(notificationCommandPort, never()).save(any(Notification.class));
    }

    @Test
    void subscribe_실시간포트에_위임한다() {
        SseEmitter emitter = new SseEmitter();

        when(notificationRealtimePort.subscribe(1L)).thenReturn(emitter);

        SseEmitter result = notificationService.subscribe(1L);

        assertSame(emitter, result);
    }
}
