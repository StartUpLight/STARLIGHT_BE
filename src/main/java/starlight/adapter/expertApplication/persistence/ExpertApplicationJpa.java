package starlight.adapter.expertApplication.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.expertApplication.required.ExpertApplicationQueryPort;
import starlight.domain.expertApplication.entity.ExpertApplication;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertApplicationJpa implements ExpertApplicationQueryPort,
        starlight.application.backoffice.expert.required.BackofficeExpertApplicationCountLookupPort,
        starlight.application.expert.required.ExpertApplicationCountLookupPort,
        starlight.application.expertReport.required.ExpertApplicationCountLookupPort {

    private final ExpertApplicationRepository repository;

    @Override
    public Boolean existsByExpertIdAndBusinessPlanId(Long expertId, Long businessPlanId) {
        try {
            return repository.existsByExpertIdAndBusinessPlanId(expertId, businessPlanId);
        } catch (Exception e) {
            log.error("전문가 신청 존재 여부 조회 중 오류가 발생했습니다.", e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.EXPERT_APPLICATION_QUERY_ERROR);
        }
    }

    @Override
    public ExpertApplication save(ExpertApplication application) {
        return repository.save(application);
    }

    @Override
    public Map<Long, Long> countByExpertIds(List<Long> expertIds) {
        try {
            if (expertIds == null || expertIds.isEmpty()) {
                return Collections.emptyMap();
            }

            return repository.countByExpertIds(expertIds).stream()
                    .collect(Collectors.toMap(
                            ExpertApplicationRepository.ExpertIdCountProjection::getExpertId,
                            p -> (long) p.getCount()
                    ));
        } catch (Exception e) {
            log.error("전문가별 신청 건수 조회 중 오류가 발생했습니다.", e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.EXPERT_APPLICATION_QUERY_ERROR);
        }
    }

    @Override
    public Map<Long, Long> countByExpertIdAndBusinessPlanIds(Long expertId, List<Long> businessPlanIds) {
        try {
            if (expertId == null) {
                return Collections.emptyMap();
            }
            if (businessPlanIds == null || businessPlanIds.isEmpty()) {
                return Collections.emptyMap();
            }

            return repository.countByExpertIdAndBusinessPlanIds(expertId, businessPlanIds).stream()
                    .collect(Collectors.toMap(
                            ExpertApplicationRepository.BusinessPlanIdCountProjection::getBusinessPlanId,
                            p -> (long) p.getCount()
                    ));
        } catch (Exception e) {
            log.error("사업계획서별 신청 건수 조회 중 오류가 발생했습니다.", e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.EXPERT_APPLICATION_QUERY_ERROR);
        }
    }
}
