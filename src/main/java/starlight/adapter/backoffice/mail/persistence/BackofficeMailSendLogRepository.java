package starlight.adapter.backoffice.mail.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.backoffice.mail.BackofficeMailSendLog;

public interface BackofficeMailSendLogRepository extends JpaRepository<BackofficeMailSendLog, Long> {
}
