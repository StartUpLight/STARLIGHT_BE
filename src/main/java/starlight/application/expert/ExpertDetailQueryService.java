package starlight.application.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expert.provided.ExpertDetailQueryUseCase;
import starlight.application.expert.provided.dto.ExpertDetailResult;
import starlight.application.expert.required.ExpertApplicationCountLookupPort;
import starlight.application.expert.required.ExpertQueryPort;
import starlight.domain.expert.enumerate.ExpertActiveStatus;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertDetailQueryService implements ExpertDetailQueryUseCase {

    private final ExpertQueryPort expertQueryPort;
    private final ExpertApplicationCountLookupPort expertApplicationLookupPort;

    @Override
    public List<ExpertDetailResult> searchAllActive() {
        List<Expert> experts = expertQueryPort.findAllWithCareersTagsCategories();

        List<Expert> activeExperts = experts.stream()
                .filter(expert -> expert.getActiveStatus() == ExpertActiveStatus.ACTIVE)
                .toList();

        List<Long> expertIds = activeExperts.stream()
                .map(Expert::getId)
                .toList();

        Map<Long, Long> countMap = expertApplicationLookupPort.countByExpertIds(expertIds);

        return activeExperts.stream()
                .map(expert -> ExpertDetailResult.from(expert, countMap.getOrDefault(expert.getId(), 0L)))
                .toList();
    }

    @Override
    public ExpertDetailResult findById(Long expertId) {
        Expert expert = expertQueryPort.findByIdWithCareersAndTags(expertId);
        if (expert.getActiveStatus() != ExpertActiveStatus.ACTIVE) {
            throw new ExpertException(ExpertErrorType.EXPERT_NOT_ACTIVE);
        }
        Map<Long, Long> countMap = expertApplicationLookupPort.countByExpertIds(List.of(expertId));
        long count = countMap.getOrDefault(expertId, 0L);
        return ExpertDetailResult.from(expert, count);
    }
}
