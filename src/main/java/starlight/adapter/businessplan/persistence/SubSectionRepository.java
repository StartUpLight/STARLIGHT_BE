package starlight.adapter.businessplan.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.Optional;

public interface SubSectionRepository extends JpaRepository<SubSection, Long> {

    Optional<SubSection> findByBusinessPlanIdAndSubSectionName(Long businessPlanId, SubSectionName subSectionName);
}
