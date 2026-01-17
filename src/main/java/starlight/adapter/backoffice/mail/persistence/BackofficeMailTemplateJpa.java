package starlight.adapter.backoffice.mail.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.mail.required.BackofficeMailTemplateCommandPort;
import starlight.application.backoffice.mail.required.BackofficeMailTemplateQueryPort;
import starlight.domain.backoffice.mail.BackofficeMailTemplate;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BackofficeMailTemplateJpa implements BackofficeMailTemplateCommandPort, BackofficeMailTemplateQueryPort {

    private final BackofficeMailTemplateRepository repository;

    @Override
    public BackofficeMailTemplate save(BackofficeMailTemplate template) {
        return repository.save(template);
    }

    @Override
    public void deleteById(Long templateId) {
        repository.deleteById(templateId);
    }

    @Override
    public List<BackofficeMailTemplate> findAll() {
        return repository.findAll();
    }
}
