package starlight.application.backoffice.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import starlight.application.backoffice.mail.event.BackofficeMailSendEvent;
import starlight.application.backoffice.mail.required.BackofficeMailSendLogCommandPort;
import starlight.application.backoffice.mail.util.EmailMaskingUtils;
import starlight.domain.backoffice.mail.BackofficeMailSendLog;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackofficeMailSendLogEventHandler {

    private final BackofficeMailSendLogCommandPort logCommandPort;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handle(BackofficeMailSendEvent event) {
        String recipients = EmailMaskingUtils.maskRecipients(event.to());

        BackofficeMailSendLog mailSendLog = BackofficeMailSendLog.create(
                recipients,
                event.subject(),
                event.contentType(),
                event.success(),
                event.errorMessage()
        );

        try {
            logCommandPort.save(mailSendLog);
        } catch (DataAccessException exception) {
            log.warn("[MAIL] send log save failed. subject={}", event.subject(), exception);
        }
    }
}
