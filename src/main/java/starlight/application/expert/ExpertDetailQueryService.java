package starlight.application.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expert.provided.ExpertDetailQueryUseCase;
import starlight.application.expert.provided.dto.ExpertDetailResult;
import starlight.application.expert.required.ExpertApplicationLookupPort;
import starlight.application.expert.required.ExpertQueryPort;
import starlight.domain.expert.entity.Expert;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertDetailQueryService implements ExpertDetailQueryUseCase {

    private final ExpertQueryPort expertQueryPort;
    private final ExpertApplicationLookupPort expertApplicationLookupPort;

    @Override
    public List<ExpertDetailResult> searchAll() {
        List<Expert> experts = expertQueryPort.findAllWithCareersTagsCategories();

        List<Long> expertIds = experts.stream()
                .map(Expert::getId)
                .toList();

        Map<Long, Long> countMap = expertApplicationLookupPort.countByExpertIds(expertIds);

        return experts.stream()
                .map(expert -> ExpertDetailResult.from(expert, countMap.getOrDefault(expert.getId(), 0L)))
                .toList();
    }

    @Override
    public ExpertDetailResult findById(Long expertId) {
        Expert expert = expertQueryPort.findByIdWithCareersAndTags(expertId);
        Map<Long, Long> countMap = expertApplicationLookupPort.countByExpertIds(List.of(expertId));
        long count = countMap.getOrDefault(expertId, 0L);
        return ExpertDetailResult.from(expert, count);
    }
}
