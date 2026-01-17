package starlight.domain.backoffice.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.shared.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BackofficeMailSendLog extends AbstractEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recipients;

    @Column(nullable = false)
    private String emailTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackofficeMailContentType contentType;

    @Column(nullable = false)
    private boolean success;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public static BackofficeMailSendLog create(
            String recipients, String emailTitle, BackofficeMailContentType contentType, boolean success, String errorMessage
    ) {
        Assert.hasText(recipients, "recipients must not be empty");
        Assert.hasText(emailTitle, "subject must not be empty");
        Assert.notNull(contentType, "contentType must not be null");

        BackofficeMailSendLog log = new BackofficeMailSendLog();
        log.recipients = recipients;
        log.emailTitle = emailTitle;
        log.contentType = contentType;
        log.success = success;
        log.errorMessage = errorMessage;

        return log;
    }
}
