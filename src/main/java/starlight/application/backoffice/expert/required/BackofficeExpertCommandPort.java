package starlight.application.backoffice.expert.required;

import starlight.domain.expert.entity.Expert;

public interface BackofficeExpertCommandPort {

    Expert save(Expert expert);

    void delete(Expert expert);
}
