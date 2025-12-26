package starlight.application.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expert.provided.ExpertDetailQueryUseCase;
import starlight.application.expert.provided.dto.ExpertDetailResult;
import starlight.application.expert.required.ExpertApplicationCountPort;
import starlight.application.expert.required.ExpertQueryPort;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertDetailQueryService implements ExpertDetailQueryUseCase {

    private final ExpertQueryPort expertQuery;
    private final ExpertApplicationCountPort expertApplicationCountQuery;

    @Override
    public List<ExpertDetailResult> search(Set<TagCategory> categories) {
        List<Expert> experts = (categories == null || categories.isEmpty())
                ? expertQuery.findAllWithDetails()
                : expertQuery.findByAllCategories(categories);

        List<Long> expertIds = experts.stream()
                .map(Expert::getId)
                .toList();

        Map<Long, Long> countMap = expertApplicationCountQuery.countByExpertIds(expertIds);

        return experts.stream()
                .map(expert -> ExpertDetailResult.from(expert, countMap.getOrDefault(expert.getId(), 0L)))
                .toList();
    }
}
