package starlight.adapter.expertApplication.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expertApplication.entity.ExpertApplication;

import java.util.List;

public interface ExpertApplicationRepository extends JpaRepository<ExpertApplication, Long> {

    Boolean existsByExpertIdAndBusinessPlanId(Long mentorId, Long businessPlanId);

    interface ExpertIdCountProjection {
        Long getExpertId();
        long getCount();
    }

    @Query("""
        select e.expertId as expertId, count(e) as count
        from ExpertApplication e
        where e.expertId in :expertIds
        group by e.expertId
    """)
    List<ExpertIdCountProjection> countByExpertIds(@Param("expertIds") List<Long> expertIds);

    interface BusinessPlanIdCountProjection {
        Long getBusinessPlanId();
        long getCount();
    }

    @Query("""
        select e.businessPlanId as businessPlanId, count(e) as count
        from ExpertApplication e
        where e.expertId = :expertId
          and e.businessPlanId in :businessPlanIds
        group by e.businessPlanId
    """)
    List<BusinessPlanIdCountProjection> countByExpertIdAndBusinessPlanIds(
            @Param("expertId") Long expertId,
            @Param("businessPlanIds") List<Long> businessPlanIds
    );
}
