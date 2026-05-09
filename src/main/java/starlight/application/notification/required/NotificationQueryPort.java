package starlight.application.notification.required;

import starlight.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationQueryPort {

    Notification findByIdOrThrow(Long notificationId);

    List<Notification> findAllByMemberIdOrderByIdDesc(Long memberId);
}
