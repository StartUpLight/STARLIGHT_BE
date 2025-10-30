package starlight.adapter.businessplan.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.Optional;

public interface BusinessPlanRepository extends JpaRepository<BusinessPlan, Long> {

    @EntityGraph(attributePaths = { "feasibility", "problemRecognition", "growthTactic", "teamCompetence", "overview" })
    Optional<BusinessPlan> findById(Long id);

    @Query("""
            SELECT s FROM SubSection s
            WHERE s.parentSectionId = :parentSectionId
            AND s.subSectionName = :subSectionName
            """)
    Optional<SubSection> findSubSectionByParentSectionIdAndName(
            @Param("parentSectionId") Long parentSectionId,
            @Param("subSectionName") SubSectionName subSectionName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM SubSection s
            WHERE s.parentSectionId = :parentSectionId
            """)
    void deleteSubSectionsByParentSectionId(@Param("parentSectionId") Long parentSectionId);
}
