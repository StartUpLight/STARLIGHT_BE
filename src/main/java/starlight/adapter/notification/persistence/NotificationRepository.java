package starlight.adapter.notification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByMemberIdOrderByIdDesc(Long memberId);
}
