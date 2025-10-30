package starlight.adapter.businessplan.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.Optional;

public interface SubSectionRepository extends JpaRepository<SubSection, Long> {

    @Query("""
                SELECT s FROM SubSection s
                WHERE s.parentSectionId = :businessPlanId
                AND s.subSectionName = :subSectionName
            """)
    Optional<SubSection> findByBusinessPlanIdAndSubSectionName(
            @Param("businessPlanId") Long businessPlanId,
            @Param("subSectionName") SubSectionName subSectionName);
}
