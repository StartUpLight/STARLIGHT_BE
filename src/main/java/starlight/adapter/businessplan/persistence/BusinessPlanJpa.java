package starlight.adapter.businessplan.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import starlight.application.businessplan.required.BusinessPlanCommandPort;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.expert.required.BusinessPlanLookupPort;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BusinessPlanJpa implements BusinessPlanCommandPort, BusinessPlanQueryPort, BusinessPlanLookupPort {

    private final BusinessPlanRepository businessPlanRepository;

    @Override
    public BusinessPlan findByIdOrThrow(Long id) {
        return businessPlanRepository.findById(id).orElseThrow(
                () -> new BusinessPlanException(BusinessPlanErrorType.BUSINESS_PLAN_NOT_FOUND)
        );
    }

    @Override
    public BusinessPlan findWithAllSubSectionsOrThrow(Long id) {
        return businessPlanRepository.findByIdWithAllSubSections(id).orElseThrow(
                () -> new BusinessPlanException(BusinessPlanErrorType.BUSINESS_PLAN_NOT_FOUND)
        );
    }

    @Override
    public BusinessPlan save(BusinessPlan businessPlan) {
        return businessPlanRepository.save(businessPlan);
    }

    @Override
    public void delete(BusinessPlan businessPlan) {
        businessPlanRepository.delete(businessPlan);
    }

    @Override
    public Page<BusinessPlan> findPreviewPage(Long memberId, Pageable pageable) {
        return businessPlanRepository.findAllByMemberIdOrderedByLastSavedAt(memberId, pageable);
    }

    @Override
    public List<BusinessPlan> findAllByMemberId(Long memberId) {
        return businessPlanRepository.findAllByMemberIdOrderByLastSavedAt(memberId);
    }
}
