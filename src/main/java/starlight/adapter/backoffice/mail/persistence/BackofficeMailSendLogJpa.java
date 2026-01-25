package starlight.adapter.backoffice.mail.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.mail.required.BackofficeMailSendLogCommandPort;
import starlight.domain.backoffice.mail.BackofficeMailSendLog;

@Repository
@RequiredArgsConstructor
public class BackofficeMailSendLogJpa implements BackofficeMailSendLogCommandPort {

    private final BackofficeMailSendLogRepository repository;

    @Override
    public BackofficeMailSendLog save(BackofficeMailSendLog log) {
        return repository.save(log);
    }
}
