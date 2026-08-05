package starlight.adapter.notification.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByMemberIdOrderByIdDesc(Long memberId);

    @Query("""
            select notification
            from Notification notification
            where notification.memberId = :memberId
              and notification.id > :notificationId
            order by notification.id asc
            """)
    List<Notification> findAllByMemberIdAndIdGreaterThanOrderByIdAsc(
            @Param("memberId") Long memberId,
            @Param("notificationId") Long notificationId,
            Pageable pageable
    );
}
