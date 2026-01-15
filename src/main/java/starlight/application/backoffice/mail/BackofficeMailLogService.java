package starlight.application.backoffice.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.backoffice.mail.provided.BackofficeMailLogUseCase;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendLogCreateInput;
import starlight.application.backoffice.mail.provided.dto.result.BackofficeMailSendLogResult;
import starlight.application.backoffice.mail.required.BackofficeMailSendLogCommandPort;
import starlight.domain.backoffice.exception.BackofficeErrorType;
import starlight.domain.backoffice.exception.BackofficeException;
import starlight.domain.backoffice.mail.BackofficeMailSendLog;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackofficeMailLogService implements BackofficeMailLogUseCase {

    private final BackofficeMailSendLogCommandPort logCommandPort;

    @Override
    @Transactional
    public BackofficeMailSendLogResult createLog(BackofficeMailSendLogCreateInput input) {
        String recipients = input.to().stream().collect(Collectors.joining(","));

        BackofficeMailSendLog log = BackofficeMailSendLog.create(
                recipients,
                input.subject(),
                input.contentType(),
                input.success(),
                input.errorMessage()
        );

        try {
            BackofficeMailSendLog saved = logCommandPort.save(log);
            return toResult(saved);
        } catch (DataAccessException exception) {
            throw new BackofficeException(BackofficeErrorType.MAIL_LOG_SAVE_FAILED);
        }
    }

    private BackofficeMailSendLogResult toResult(BackofficeMailSendLog log) {
        return new BackofficeMailSendLogResult(
                log.getId(),
                log.getRecipients(),
                log.getEmailTitle(),
                log.getContentType().name().toLowerCase(),
                log.isSuccess(),
                log.getErrorMessage(),
                log.getCreatedAt()
        );
    }
}
