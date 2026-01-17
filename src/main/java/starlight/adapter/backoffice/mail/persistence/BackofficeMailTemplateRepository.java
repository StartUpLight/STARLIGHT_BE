package starlight.adapter.backoffice.mail.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.backoffice.mail.BackofficeMailTemplate;

public interface BackofficeMailTemplateRepository extends JpaRepository<BackofficeMailTemplate, Long> {
}
