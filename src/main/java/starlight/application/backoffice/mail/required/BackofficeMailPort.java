package starlight.application.backoffice.mail.required;

import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;
import starlight.domain.backoffice.mail.BackofficeMailContentType;

public interface BackofficeMailPort {

    void send(BackofficeMailSendInput input, BackofficeMailContentType contentType);
}
