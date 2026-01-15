package starlight.application.backoffice.mail.provided;

import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendLogCreateInput;
import starlight.application.backoffice.mail.provided.dto.result.BackofficeMailSendLogResult;

public interface BackofficeMailLogUseCase {

    BackofficeMailSendLogResult createLog(BackofficeMailSendLogCreateInput input);
}
