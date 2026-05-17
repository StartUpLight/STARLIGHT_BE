package starlight.application.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.event.NotificationCreatedEvent;
import starlight.application.notification.provided.dto.input.NotificationSendInput;
import starlight.application.notification.provided.dto.result.NotificationResult;
import starlight.application.notification.required.NotificationCommandPort;
import starlight.application.notification.required.NotificationOutboxCommandPort;
import starlight.application.notification.required.NotificationQueryPort;
import starlight.application.notification.required.NotificationRealtimePort;
import starlight.application.notification.required.dto.NotificationPublishMessage;
import starlight.domain.notification.entity.Notification;
import starlight.domain.notification.entity.NotificationOutbox;
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
    private NotificationOutboxCommandPort notificationOutboxCommandPort;

    @Mock
    private NotificationQueryPort notificationQueryPort;

    @Mock
    private NotificationRealtimePort notificationRealtimePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "sseRecoveryLimit", 100);
    }

    @Test
    void notifyMember_저장후_이벤트를_발행한다() {
        NotificationSendInput input = NotificationSendInput.of(
                1L, "SYSTEM", "title", "message", 99L
        );
        Notification savedNotification = Notification.create(1L, "SYSTEM", "title", "message", 99L);
        ReflectionTestUtils.setField(savedNotification, "id", 10L);

        when(notificationCommandPort.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationOutboxCommandPort.save(any(NotificationOutbox.class))).thenAnswer(invocation -> {
            NotificationOutbox outbox = invocation.getArgument(0);
            ReflectionTestUtils.setField(outbox, "id", 20L);
            return outbox;
        });

        notificationService.notifyMember(input);

        verify(notificationCommandPort).save(any(Notification.class));
        verify(notificationOutboxCommandPort).save(any(NotificationOutbox.class));
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

        when(notificationRealtimePort.subscribe(1L, List.of())).thenReturn(emitter);

        SseEmitter result = notificationService.subscribe(1L, null);

        assertSame(emitter, result);
    }

    @Test
    void subscribe_lastEventId가_있으면_누락알림을_조회해_전달한다() {
        SseEmitter emitter = new SseEmitter();
        Notification notification = Notification.create(1L, "SYSTEM", "title", "message", null);
        ReflectionTestUtils.setField(notification, "id", 11L);

        when(notificationQueryPort.findAllByMemberIdAndIdGreaterThanOrderByIdAsc(1L, 10L, 100))
                .thenReturn(List.of(notification));
        when(notificationRealtimePort.subscribe(eq(1L), anyList())).thenReturn(emitter);

        SseEmitter result = notificationService.subscribe(1L, 10L);

        assertSame(emitter, result);
        verify(notificationQueryPort).findAllByMemberIdAndIdGreaterThanOrderByIdAsc(1L, 10L, 100);
        verify(notificationRealtimePort).subscribe(eq(1L), argThat(messages ->
                messages.size() == 1 && messages.getFirst().notificationId().equals(11L)
        ));
    }

    @Test
    void subscribe_lastEventId가_0이하면_누락알림을_조회하지_않는다() {
        SseEmitter emitter = new SseEmitter();

        when(notificationRealtimePort.subscribe(1L, List.of())).thenReturn(emitter);

        SseEmitter result = notificationService.subscribe(1L, 0L);

        assertSame(emitter, result);
        verify(notificationQueryPort, never())
                .findAllByMemberIdAndIdGreaterThanOrderByIdAsc(anyLong(), anyLong(), anyInt());
    }

    @Test
    void sendRealtime_구독자가_없으면_DB를_조회하지_않는다() {
        NotificationPublishMessage message = new NotificationPublishMessage(1L, 10L, "SYSTEM");

        when(notificationRealtimePort.hasSubscriber(1L)).thenReturn(false);

        notificationService.sendRealtime(message);

        verify(notificationQueryPort, never()).findByIdOrThrow(anyLong());
        verify(notificationRealtimePort, never()).send(any());
    }

    @Test
    void sendRealtime_구독자가_있으면_DB조회후_실시간전송한다() {
        NotificationPublishMessage message = new NotificationPublishMessage(1L, 10L, "SYSTEM");
        Notification notification = Notification.create(1L, "SYSTEM", "title", "message", null);
        ReflectionTestUtils.setField(notification, "id", 10L);

        when(notificationRealtimePort.hasSubscriber(1L)).thenReturn(true);
        when(notificationQueryPort.findByIdOrThrow(10L)).thenReturn(notification);

        notificationService.sendRealtime(message);

        verify(notificationQueryPort).findByIdOrThrow(10L);
        verify(notificationRealtimePort).send(argThat(realtimeMessage ->
                realtimeMessage.notificationId().equals(10L)
                        && realtimeMessage.title().equals("title")
                        && realtimeMessage.message().equals("message")
        ));
    }
}
