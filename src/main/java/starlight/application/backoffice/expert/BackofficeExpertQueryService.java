package starlight.application.backoffice.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.backoffice.expert.provided.BackofficeExpertQueryUseCase;
import starlight.application.backoffice.expert.provided.dto.result.BackofficeExpertDetailResult;
import starlight.application.backoffice.expert.required.BackofficeExpertApplicationCountLookupPort;
import starlight.application.backoffice.expert.required.BackofficeExpertQueryPort;
import starlight.domain.expert.entity.Expert;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeExpertQueryService implements BackofficeExpertQueryUseCase {

    private final BackofficeExpertQueryPort expertQueryPort;
    private final BackofficeExpertApplicationCountLookupPort expertApplicationLookupPort;

    @Override
    public List<BackofficeExpertDetailResult> searchAll() {
        List<Expert> experts = expertQueryPort.findAllWithCareersTagsCategories();

        List<Long> expertIds = experts.stream()
                .map(Expert::getId)
                .toList();

        Map<Long, Long> countMap = expertApplicationLookupPort.countByExpertIds(expertIds);

        return experts.stream()
                .map(expert -> BackofficeExpertDetailResult.from(expert, countMap.getOrDefault(expert.getId(), 0L)))
                .toList();
    }
}
