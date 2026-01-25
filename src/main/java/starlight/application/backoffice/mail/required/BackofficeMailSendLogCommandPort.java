package starlight.application.backoffice.mail.required;

import starlight.domain.backoffice.mail.BackofficeMailSendLog;

public interface BackofficeMailSendLogCommandPort {

    BackofficeMailSendLog save(BackofficeMailSendLog log);
}
