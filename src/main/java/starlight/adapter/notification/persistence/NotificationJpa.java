package starlight.adapter.notification.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import starlight.application.notification.required.NotificationCommandPort;
import starlight.application.notification.required.NotificationQueryPort;
import starlight.domain.notification.entity.Notification;
import starlight.domain.notification.exception.NotificationErrorType;
import starlight.domain.notification.exception.NotificationException;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationJpa implements NotificationCommandPort, NotificationQueryPort {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public Notification findByIdOrThrow(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorType.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public List<Notification> findAllByMemberIdOrderByIdDesc(Long memberId) {
        return notificationRepository.findAllByMemberIdOrderByIdDesc(memberId);
    }

    @Override
    public List<Notification> findAllByMemberIdAndIdGreaterThanOrderByIdAsc(
            Long memberId,
            Long notificationId,
            int limit
    ) {
        return notificationRepository.findAllByMemberIdAndIdGreaterThanOrderByIdAsc(
                memberId,
                notificationId,
                PageRequest.of(0, limit)
        );
    }
}
