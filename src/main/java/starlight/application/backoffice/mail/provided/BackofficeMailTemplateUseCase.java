package starlight.application.backoffice.mail.provided;

import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailTemplateCreateInput;
import starlight.application.backoffice.mail.provided.dto.result.BackofficeMailTemplateResult;

import java.util.List;

public interface BackofficeMailTemplateUseCase {

    BackofficeMailTemplateResult createTemplate(BackofficeMailTemplateCreateInput input);

    List<BackofficeMailTemplateResult> findTemplates();

    void deleteTemplate(Long templateId);
}
