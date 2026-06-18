package starlight.adapter.notification.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.notification.entity.NotificationOutbox;
import starlight.domain.notification.enumerate.NotificationOutboxStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Query("""
            select outbox
            from NotificationOutbox outbox
            where outbox.retryCount < :maxRetryCount
              and (
                (outbox.status in :readyStatuses and outbox.nextRetryAt <= :now)
                or (
                  outbox.status = :processingStatus
                  and outbox.processingStartedAt <= :processingTimeoutThreshold
                )
              )
            order by outbox.id asc
            """)
    List<NotificationOutbox> findPublishableOutboxes(
            @Param("readyStatuses") Collection<NotificationOutboxStatus> readyStatuses,
            @Param("processingStatus") NotificationOutboxStatus processingStatus,
            @Param("now") LocalDateTime now,
            @Param("processingTimeoutThreshold") LocalDateTime processingTimeoutThreshold,
            @Param("maxRetryCount") int maxRetryCount,
            Pageable pageable
    );
}
