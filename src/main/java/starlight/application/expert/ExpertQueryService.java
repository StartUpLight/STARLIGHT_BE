package starlight.application.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expert.provided.ExpertQueryUseCase;
import starlight.application.expert.required.ExpertQueryPort;
import starlight.domain.expert.entity.Expert;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertQueryService implements ExpertQueryUseCase {

    private final ExpertQueryPort expertQuery;

    @Override
    public Expert findByIdWithDetails(Long id) {
        return expertQuery.findByIdWithDetails(id);
    }

    @Override
    public Map<Long, Expert> findByIds(Set<Long> expertIds) {
        return expertQuery.findExpertMapByIds(expertIds);
    }
}
