package starlight.application.expert.provided;

import starlight.domain.expert.entity.Expert;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExpertQueryUseCase {

    Expert findByIdWithDetails(Long id);

    Map<Long, Expert> findByIds(Set<Long> expertIds);
}
