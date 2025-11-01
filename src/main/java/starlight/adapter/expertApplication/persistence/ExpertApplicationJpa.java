package starlight.adapter.expertApplication.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.expert.required.ExpertQuery;
import starlight.application.expertApplicaiton.provided.ExpertApplicationFinder;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;
import starlight.domain.expertApplication.entity.ExpertApplication;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertApplicationJpa implements ExpertApplicationFinder {

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
