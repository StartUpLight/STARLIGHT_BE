package starlight.adapter.expert.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.expert.required.ExpertQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertJpa implements ExpertQuery {

    private final ExpertRepository repository;

    @Override
    public Expert getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND)
        );
    }

    @Override
    public List<Expert> findAllWithDetails() {
        try {
            return repository.findAllWithDetails();
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
    public Map<Long, Expert> findByIds(List<Long> expertIds) {

        List<Expert> experts = repository.findAllById(expertIds);

        return experts.stream()
                .collect(Collectors.toMap(Expert::getId, Function.identity()));
    }
}
