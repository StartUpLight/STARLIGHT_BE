package starlight.application.backoffice.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.backoffice.mail.provided.BackofficeMailTemplateUseCase;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailTemplateCreateInput;
import starlight.application.backoffice.mail.provided.dto.result.BackofficeMailTemplateResult;
import starlight.application.backoffice.mail.required.BackofficeMailTemplateCommandPort;
import starlight.application.backoffice.mail.required.BackofficeMailTemplateQueryPort;
import starlight.domain.backoffice.exception.BackofficeErrorType;
import starlight.domain.backoffice.exception.BackofficeException;
import starlight.domain.backoffice.mail.BackofficeMailContentType;
import starlight.domain.backoffice.mail.BackofficeMailTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BackofficeMailTemplateService implements BackofficeMailTemplateUseCase {

    private final BackofficeMailTemplateCommandPort templateCommandPort;
    private final BackofficeMailTemplateQueryPort templateQueryPort;

    @Override
    @Transactional
    public BackofficeMailTemplateResult createTemplate(BackofficeMailTemplateCreateInput input) {
        BackofficeMailContentType contentType = parseContentType(input.contentType());
        BackofficeMailTemplate template = BackofficeMailTemplate.create(
                input.name(),
                input.title(),
                contentType,
                input.html(),
                input.text()
        );

        try {
            BackofficeMailTemplate saved = templateCommandPort.save(template);
            return toResult(saved);
        } catch (DataAccessException exception) {
            throw new BackofficeException(BackofficeErrorType.MAIL_TEMPLATE_SAVE_FAILED);
        }
    }

    private BackofficeMailContentType parseContentType(String contentType) {
        try {
            return BackofficeMailContentType.from(contentType);
        } catch (IllegalArgumentException exception) {
            throw new BackofficeException(BackofficeErrorType.INVALID_MAIL_CONTENT_TYPE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BackofficeMailTemplateResult> findTemplates() {
        try {
            return templateQueryPort.findAll().stream()
                    .map(this::toResult)
                    .toList();
        } catch (DataAccessException exception) {
            throw new BackofficeException(BackofficeErrorType.MAIL_TEMPLATE_QUERY_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateId) {
        try {
            templateCommandPort.deleteById(templateId);
        } catch (DataAccessException exception) {
            throw new BackofficeException(BackofficeErrorType.MAIL_TEMPLATE_DELETE_FAILED);
        }
    }

    private BackofficeMailTemplateResult toResult(BackofficeMailTemplate template) {
        return BackofficeMailTemplateResult.of(
                template.getId(),
                template.getName(),
                template.getEmailTitle(),
                template.getContentType().name().toLowerCase(),
                template.getHtml(),
                template.getText(),
                template.getCreatedAt()
        );
    }
}
