package starlight.application.expertApplication.required;

import starlight.domain.expert.entity.Expert;

public interface ExpertLookupPort {

    Expert findByIdOrThrow(Long id);
}
