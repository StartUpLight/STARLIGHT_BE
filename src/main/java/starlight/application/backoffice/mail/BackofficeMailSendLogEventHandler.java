package starlight.application.backoffice.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import starlight.application.backoffice.mail.event.BackofficeMailSendEvent;
import starlight.application.backoffice.mail.required.BackofficeMailSendLogCommandPort;
import starlight.application.backoffice.mail.util.EmailMaskingUtils;
import starlight.domain.backoffice.exception.BackofficeErrorType;
import starlight.domain.backoffice.exception.BackofficeException;
import starlight.domain.backoffice.mail.BackofficeMailSendLog;

@Component
@RequiredArgsConstructor
public class BackofficeMailSendLogEventHandler {

    private final BackofficeMailSendLogCommandPort logCommandPort;

    @EventListener
    public void handle(BackofficeMailSendEvent event) {
        String recipients = EmailMaskingUtils.maskRecipients(event.to());

        BackofficeMailSendLog log = BackofficeMailSendLog.create(
                recipients,
                event.subject(),
                event.contentType(),
                event.success(),
                event.errorMessage()
        );

        try {
            logCommandPort.save(log);
        } catch (DataAccessException exception) {
            throw new BackofficeException(BackofficeErrorType.MAIL_LOG_SAVE_FAILED);
        }
    }
}
