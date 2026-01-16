package starlight.application.backoffice.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import starlight.application.backoffice.mail.event.BackofficeMailSendEvent;
import starlight.application.backoffice.mail.required.BackofficeMailSendLogCommandPort;
import starlight.domain.backoffice.mail.BackofficeMailSendLog;

@Component
@RequiredArgsConstructor
public class BackofficeMailSendLogEventHandler {

    private final BackofficeMailSendLogCommandPort logCommandPort;

    @EventListener
    public void handle(BackofficeMailSendEvent event) {
        String recipients = String.join(",", event.to());
        BackofficeMailSendLog log = BackofficeMailSendLog.create(
                recipients,
                event.subject(),
                event.contentType(),
                event.success(),
                event.errorMessage()
        );
        logCommandPort.save(log);
    }
}
