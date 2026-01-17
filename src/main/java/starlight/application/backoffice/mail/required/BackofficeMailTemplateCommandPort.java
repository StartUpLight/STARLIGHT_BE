package starlight.application.backoffice.mail.required;

import starlight.domain.backoffice.mail.BackofficeMailTemplate;

public interface BackofficeMailTemplateCommandPort {

    BackofficeMailTemplate save(BackofficeMailTemplate template);

    void deleteById(Long templateId);
}
