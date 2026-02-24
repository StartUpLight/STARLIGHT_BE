package starlight.adapter.expert.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.backoffice.expertapplication.required.ExpertLookupPort;
import starlight.application.expert.required.ExpertQueryPort;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertJpa implements ExpertQueryPort,
        ExpertLookupPort,
        starlight.application.backoffice.expert.required.BackofficeExpertQueryPort,
        starlight.application.backoffice.expert.required.BackofficeExpertCommandPort,
        starlight.application.expertReport.required.ExpertLookupPort,
        starlight.application.expertApplication.required.ExpertLookupPort {

    private final ExpertRepository repository;

    @Override
    public Expert findByIdOrThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND)
        );
    }

    @Override
    public Expert findByIdWithCareersTagsCategories(Long id) {
        try {
            List<Expert> experts = fetchWithCollections(List.of(id));
            if (experts.isEmpty()) {
                throw new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND);
            }
            return experts.get(0);
        } catch (ExpertException e) {
            throw e;
        } catch (Exception e) {
            log.error("전문가 상세 조회 중 오류가 발생했습니다.", e);
            throw new ExpertException(ExpertErrorType.EXPERT_QUERY_ERROR);
        }
    }

    @Override
    public Expert save(Expert expert) {
        return repository.save(expert);
    }

    @Override
    public void delete(Expert expert) {
        repository.delete(expert);
    }

    @Override
    public Expert findByIdWithCareersAndTags(Long id) {
        try {
            List<Expert> experts = fetchWithCollections(List.of(id));
            if (experts.isEmpty()) {
                throw new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND);
            }

            return experts.get(0);
        } catch (ExpertException e) {
            throw e;
        } catch (Exception e) {
            log.error("전문가 상세 조회 중 오류가 발생했습니다.", e);
            throw new ExpertException(ExpertErrorType.EXPERT_QUERY_ERROR);
        }
    }

    @Override
    public List<Expert> findAllWithCareersTagsCategories() {
        try {
            List<Long> ids = repository.findAllIds();
            return fetchWithCollections(ids);
        } catch (Exception e) {
            log.error("전문가 목록 조회 중 오류가 발생했습니다.", e);
            throw new ExpertException(ExpertErrorType.EXPERT_QUERY_ERROR);
        }
    }

    @Override
    public Map<Long, Expert> findByIds(Set<Long> expertIds) {
        if (expertIds == null || expertIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Expert> experts = fetchWithCollections(expertIds.stream().toList());

        return experts.stream()
                .collect(Collectors.toMap(Expert::getId, Function.identity()));
    }

    @Override
    public Map<Long, String> findExpertNamesByIds(Collection<Long> expertIds) {
        if (expertIds == null || expertIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Expert> experts = repository.findAllByIds(Set.copyOf(expertIds));
        return experts.stream()
                .collect(Collectors.toMap(Expert::getId, Expert::getName));
    }

    private List<Expert> fetchWithCollections(List<Long> ids) {
        List<Expert> experts = repository.fetchExpertsWithCareersByIds(ids);
        repository.fetchExpertsWithTagsByIds(ids);
        repository.fetchExpertsWithCategoriesByIds(ids);
        return experts;
    }
}
