package starlight.adapter.businessplan.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.businessplan.required.SubSectionQuery;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubSectionJpa implements SubSectionQuery {

    private final SubSectionRepository subSectionRepository;

    @Override
    public Optional<SubSection> findByBusinessPlanIdAndSubSectionName(Long businessPlanId, SubSectionName subSectionName) {
        return subSectionRepository.findByBusinessPlanIdAndSubSectionName(businessPlanId, subSectionName);
    }

    @Override
    public SubSection save(SubSection subSection) {
        return subSectionRepository.save(subSection);
    }

    @Override
    public void delete(SubSection subSection) {
        subSectionRepository.delete(subSection);
    }
}


