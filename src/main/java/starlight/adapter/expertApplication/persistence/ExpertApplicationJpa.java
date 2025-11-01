package starlight.adapter.expertApplication.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.expertApplicaiton.required.ExpertApplicationQuery;
import starlight.domain.expertApplication.entity.ExpertApplication;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertApplicationJpa implements ExpertApplicationQuery {

    private final ExpertApplicationRepository repository;

    @Override
    public List<Long> findRequestedExpertIds(Long businessPlanId) {
        try {
            return repository.findRequestedExpertIdsByPlanId(businessPlanId);
        } catch (Exception e) {
            log.error("전문가 첨삭 요청 목록 조회 중 오류가 발생했습니다.", e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.EXPERT_APPLICATION_QUERY_ERROR);
        }
    }

    @Override
    public ExpertApplication save(ExpertApplication application) {
        return repository.save(application);
    }
}
