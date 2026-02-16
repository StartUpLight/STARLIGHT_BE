package starlight.adapter.businessplan.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import starlight.application.businessplan.required.BusinessPlanCommandPort;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BusinessPlanQueryJpa implements BusinessPlanCommandPort, BusinessPlanQueryPort,
        starlight.application.expert.required.BusinessPlanQueryLookupPort,
        starlight.application.aireport.required.BusinessPlanCommandLookupPort,
        starlight.application.aireport.required.BusinessPlanQueryLookupPort {

    private final BusinessPlanRepository businessPlanRepository;

    @Override
    public BusinessPlan findByIdOrThrow(Long id) {
        return businessPlanRepository.findById(id).orElseThrow(
                () -> new BusinessPlanException(BusinessPlanErrorType.BUSINESS_PLAN_NOT_FOUND)
        );
    }

    @Override
    public BusinessPlan findByIdWithAllSubSectionsOrThrow(Long id) {
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

    @Override
    public Long createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId) {
        BusinessPlan plan = BusinessPlan.createWithPdf(title, memberId, pdfUrl);
        BusinessPlan saved = businessPlanRepository.save(plan);
        return saved.getId();
    }
}
