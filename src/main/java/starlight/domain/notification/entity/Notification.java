package starlight.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.notification.exception.NotificationErrorType;
import starlight.domain.notification.exception.NotificationException;
import starlight.shared.AbstractEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(
        indexes = {
                @Index(name = "idx_notification_member_id", columnList = "member_id"),
                @Index(name = "idx_notification_member_read_id", columnList = "member_id, is_read, id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends AbstractEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public static Notification create(
            Long memberId,
            String type,
            String title,
            String message,
            Long referenceId
    ) {
        Assert.notNull(memberId, "memberId must not be null");
        Assert.hasText(type, "type must not be empty");
        Assert.hasText(title, "title must not be empty");
        Assert.hasText(message, "message must not be empty");

        Notification notification = new Notification();
        notification.memberId = memberId;
        notification.type = type;
        notification.title = title;
        notification.message = message;
        notification.referenceId = referenceId;
        notification.read = false;

        return notification;
    }

    public void validateOwner(Long memberId) {
        if (!Objects.equals(this.memberId, memberId)) {
            throw new NotificationException(NotificationErrorType.NOTIFICATION_ACCESS_DENIED);
        }
    }

    public void markAsRead() {
        if (read) {
            return;
        }

        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}
