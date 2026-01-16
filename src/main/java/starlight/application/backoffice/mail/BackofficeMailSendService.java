package starlight.application.backoffice.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.backoffice.mail.provided.BackofficeMailSendUseCase;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;
import starlight.application.backoffice.mail.required.MailSenderPort;
import starlight.application.backoffice.mail.event.BackofficeMailSendEvent;
import org.springframework.context.ApplicationEventPublisher;
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
        BackofficeMailContentType contentType = parseContentType(input.contentType());

        try {
            validate(input, contentType);
            mailSenderPort.send(input, contentType);
            eventPublisher.publishEvent(new BackofficeMailSendEvent(
                    input.to(),
                    input.subject(),
                    contentType,
                    true,
                    null
            ));
            return;
        } catch (IllegalArgumentException exception) {
            publishFailureEvent(input, contentType, exception.getMessage());
            throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_REQUEST);
        } catch (Exception exception) {
            publishFailureEvent(input, contentType, exception.getMessage());
            throw new BackofficeException(BackofficeErrorType.MAIL_SEND_FAILED);
        }
    }

    private BackofficeMailContentType parseContentType(String contentType) {
        try {
            return BackofficeMailContentType.from(contentType);
        } catch (IllegalArgumentException exception) {
            throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_CONTENT_TYPE);
        }
    }

    private void validate(BackofficeMailSendInput input, BackofficeMailContentType contentType) {
        if (input.to() == null || input.to().isEmpty()) {
            throw new IllegalArgumentException("recipient is required");
        }
        if (input.subject() == null || input.subject().isBlank()) {
            throw new IllegalArgumentException("subject is required");
        }
        if (contentType == BackofficeMailContentType.HTML) {
            if (input.html() == null || input.html().isBlank()) {
                throw new IllegalArgumentException("html body is required");
            }
        }
        if (contentType == BackofficeMailContentType.TEXT) {
            if (input.text() == null || input.text().isBlank()) {
                throw new IllegalArgumentException("text body is required");
            }
        }
    }

    private void publishFailureEvent(
            BackofficeMailSendInput input,
            BackofficeMailContentType contentType,
            String errorMessage
    ) {
        eventPublisher.publishEvent(new BackofficeMailSendEvent(
                input.to(),
                input.subject(),
                contentType,
                false,
                errorMessage
        ));
    }
}
