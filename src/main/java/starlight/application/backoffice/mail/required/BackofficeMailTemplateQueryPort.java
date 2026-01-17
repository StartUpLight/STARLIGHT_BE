package starlight.application.backoffice.mail.required;

import starlight.domain.backoffice.mail.BackofficeMailTemplate;

import java.util.List;

public interface BackofficeMailTemplateQueryPort {

    List<BackofficeMailTemplate> findAll();
}
