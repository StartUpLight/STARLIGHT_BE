package starlight.application.expertReport.required;

import starlight.domain.expert.entity.Expert;

import java.util.Map;
import java.util.Set;

public interface ExpertLookupPort {

    Expert findByIdWithDetails(Long id);

    Map<Long, Expert> findByIds(Set<Long> expertIds);
}
