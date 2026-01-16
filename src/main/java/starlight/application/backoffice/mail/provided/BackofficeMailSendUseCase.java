package starlight.application.backoffice.mail.provided;

import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;

public interface BackofficeMailSendUseCase {

    void send(BackofficeMailSendInput input);
}
