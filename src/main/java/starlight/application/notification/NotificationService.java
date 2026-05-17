package starlight.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.event.NotificationCreatedEvent;
import starlight.application.notification.provided.NotificationUseCase;
import starlight.application.notification.provided.dto.input.NotificationSendInput;
import starlight.application.notification.provided.dto.result.NotificationResult;
import starlight.application.notification.required.NotificationCommandPort;
import starlight.application.notification.required.NotificationOutboxCommandPort;
import starlight.application.notification.required.NotificationQueryPort;
import starlight.application.notification.required.NotificationRealtimePort;
import starlight.application.notification.required.dto.NotificationPublishMessage;
import starlight.domain.notification.entity.Notification;
import starlight.domain.notification.entity.NotificationOutbox;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {

    private final NotificationCommandPort notificationCommandPort;
    private final NotificationOutboxCommandPort notificationOutboxCommandPort;
    private final NotificationQueryPort notificationQueryPort;
    private final NotificationRealtimePort notificationRealtimePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void notifyMember(NotificationSendInput input) {
        Notification notification = Notification.create(
                input.memberId(),
                input.type(),
                input.title(),
                input.message(),
                input.referenceId()
        );

        Notification savedNotification = notificationCommandPort.save(notification);
        NotificationOutbox savedOutbox = notificationOutboxCommandPort.save(
                NotificationOutbox.create(savedNotification.getId(), LocalDateTime.now())
        );

        eventPublisher.publishEvent(NotificationCreatedEvent.of(savedOutbox.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResult> findAllByMemberId(Long memberId) {
        return notificationQueryPort.findAllByMemberIdOrderByIdDesc(memberId).stream()
                .map(NotificationResult::from)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        Notification notification = notificationQueryPort.findByIdOrThrow(notificationId);
        notification.validateOwner(memberId);
        notification.markAsRead();
        notificationCommandPort.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long memberId, Long lastEventId) {
        List<NotificationPublishMessage> missedMessages = findMissedMessages(memberId, lastEventId);
        return notificationRealtimePort.subscribe(memberId, missedMessages);
    }

    private List<NotificationPublishMessage> findMissedMessages(Long memberId, Long lastEventId) {
        if (lastEventId == null) {
            return List.of();
        }

        return notificationQueryPort.findAllByMemberIdAndIdGreaterThanOrderByIdAsc(memberId, lastEventId).stream()
                .map(NotificationPublishMessage::from)
                .toList();
    }
}
