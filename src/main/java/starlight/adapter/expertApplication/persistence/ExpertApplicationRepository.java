package starlight.adapter.expertApplication.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expertApplication.entity.ExpertApplication;

import java.util.List;

public interface ExpertApplicationRepository extends JpaRepository<ExpertApplication, Long> {

    @Query("""
           select distinct e.mentorId
           from ExpertApplication e
           where e.businessPlanId = :businessPlanId
           """)
    List<Long> findRequestedExpertIdsByPlanId(@Param("businessPlanId") Long businessPlanId);
}
