package starlight.adapter.expertApplication.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expertApplication.entity.ExpertApplication;

import java.util.List;

public interface ExpertApplicationRepository extends JpaRepository<ExpertApplication, Long> {

    Boolean existsByExpertIdAndBusinessPlanId(Long mentorId, Long businessPlanId);

    @Query("""
           select distinct e.expertId
           from ExpertApplication e
           where e.businessPlanId = :businessPlanId
           """)
    List<Long> findRequestedExpertIdsByPlanId(@Param("businessPlanId") Long businessPlanId);

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
}
