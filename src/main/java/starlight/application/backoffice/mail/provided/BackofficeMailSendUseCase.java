package starlight.application.backoffice.mail.provided;

import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;
import starlight.application.backoffice.mail.provided.dto.result.BackofficeMailSendLogResult;

public interface BackofficeMailSendUseCase {

    BackofficeMailSendLogResult send(BackofficeMailSendInput input);
}
