package starlight.application.backoffice.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.backoffice.mail.provided.BackofficeMailSendUseCase;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;
import starlight.application.backoffice.mail.required.MailSenderPort;
import starlight.application.backoffice.mail.util.BackofficeMailContentTypeParser;
import starlight.application.backoffice.mail.event.BackofficeMailSendEvent;
import starlight.domain.backoffice.exception.BackofficeErrorType;
import starlight.domain.backoffice.exception.BackofficeException;
import starlight.domain.backoffice.mail.BackofficeMailContentType;

@Service
@RequiredArgsConstructor
public class BackofficeMailSendService implements BackofficeMailSendUseCase {

    private final MailSenderPort mailSenderPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void send(BackofficeMailSendInput input) {
        BackofficeMailContentType contentType = BackofficeMailContentTypeParser.parse(input.contentType());

        try {
            validate(input, contentType);

            mailSenderPort.send(input, contentType);

            BackofficeMailSendEvent log = BackofficeMailSendEvent.of(
                    input.to(),
                    input.subject(),
                    contentType,
                    true,
                    null
            );
            eventPublisher.publishEvent(log);
        } catch (BackofficeException exception) {
            publishFailureEvent(input, contentType, exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            publishFailureEvent(input, contentType, exception.getMessage());
            throw new BackofficeException(BackofficeErrorType.MAIL_SEND_FAILED);
        }
    }

    private void validate(BackofficeMailSendInput input, BackofficeMailContentType contentType) {
        if (input.to() == null || input.to().isEmpty()) {
            throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_REQUEST);
        }
        if (input.subject() == null || input.subject().isBlank()) {
            throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_REQUEST);
        }
        if (contentType == BackofficeMailContentType.HTML) {
            if (input.html() == null || input.html().isBlank()) {
                throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_REQUEST);
            }
        }
        if (contentType == BackofficeMailContentType.TEXT) {
            if (input.text() == null || input.text().isBlank()) {
                throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_REQUEST);
            }
        }
    }

    private void publishFailureEvent(
            BackofficeMailSendInput input,
            BackofficeMailContentType contentType,
            String errorMessage
    ) {
        eventPublisher.publishEvent(BackofficeMailSendEvent.of(
                input.to(),
                input.subject(),
                contentType,
                false,
                errorMessage
        ));
    }
}
