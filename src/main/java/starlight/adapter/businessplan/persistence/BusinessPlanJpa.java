package starlight.adapter.businessplan.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.Optional;

import static starlight.domain.businessplan.exception.BusinessPlanErrorType.BUSINESS_PLAN_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class BusinessPlanJpa implements BusinessPlanQuery {

    private final BusinessPlanRepository businessPlanRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BusinessPlan getOrThrow(Long id) {
        return businessPlanRepository.findById(id).orElseThrow(
                () -> new BusinessPlanException(BUSINESS_PLAN_NOT_FOUND)
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
    public Optional<SubSection> findSubSectionByParentSectionIdAndName(Long parentSectionId, SubSectionName subSectionName) {
        return businessPlanRepository.findSubSectionByParentSectionIdAndName(parentSectionId, subSectionName);
    }

    @Override
    public SubSection saveSubSection(SubSection subSection) {
        if (subSection.getId() == null) {
            entityManager.persist(subSection);
            return subSection;
        }
        return entityManager.merge(subSection);
    }

    @Override
    public void deleteSubSection(SubSection subSection) {
        entityManager.remove(entityManager.contains(subSection) ? subSection : entityManager.merge(subSection));
    }

    @Override
    public void deleteSubSectionsByParentSectionId(Long parentSectionId) {
        businessPlanRepository.deleteSubSectionsByParentSectionId(parentSectionId);
    }
}
