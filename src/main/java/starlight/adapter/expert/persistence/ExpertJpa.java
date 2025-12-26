package starlight.adapter.expert.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.expert.required.ExpertQueryPort;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertJpa implements ExpertQueryPort,
        starlight.application.expertReport.required.ExpertLookupPort,
        starlight.application.expertApplication.required.ExpertLookupPort {

    private final ExpertRepository repository;

    @Override
    public Expert findById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND)
        );
    }

    @Override
    public Expert findByIdWithCareersAndTags(Long id) {
        try {
            List<Expert> experts = repository.fetchExpertsWithCareersByIds(List.of(id));
            if (experts.isEmpty()) {
                throw new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND);
            }

            repository.fetchExpertsWithTagsByIds(List.of(id));

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

            List<Expert> experts = repository.fetchExpertsWithCareersByIds(ids);
            repository.fetchExpertsWithTagsByIds(ids);
            repository.fetchExpertsWithCategoriesByIds(ids);

            return experts;
        } catch (Exception e) {
            log.error("전문가 목록 조회 중 오류가 발생했습니다.", e);
            throw new ExpertException(ExpertErrorType.EXPERT_QUERY_ERROR);
        }
    }

    @Override
    public List<Expert> findByAllCategories(Collection<TagCategory> categories) {
        try {
            return repository.findByAllCategories(categories, categories.size());
        } catch (Exception e) {
            log.error("전문가 목록 필터링 중 오류가 발생했습니다.", e);
            throw new ExpertException(ExpertErrorType.EXPERT_QUERY_ERROR);
        }
    }

    @Override
    public Map<Long, Expert> findByIds(Set<Long> expertIds) {
        List<Expert> experts = repository.findAllByIds(expertIds);

        return experts.stream()
                .collect(Collectors.toMap(Expert::getId, Function.identity()));
    }
}
