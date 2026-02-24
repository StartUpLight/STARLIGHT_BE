package starlight.adapter.backoffice.expertapplication.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.application.backoffice.expertapplication.required.dto.BackofficeExpertApplicationLookupResult;
import starlight.domain.expertApplication.entity.ExpertApplication;

import java.util.List;

public interface BackofficeBusinessPlanExpertApplicationRepository extends JpaRepository<ExpertApplication, Long> {

    @Query("""
            select new starlight.application.backoffice.expertapplication.required.dto.BackofficeExpertApplicationLookupResult(
                ea.id,
                ea.expertId,
                ea.createdAt,
                er.submitStatus,
                er.modifiedAt,
                er.overallComment
            )
            from ExpertApplication ea
            left join ExpertReport er
                on er.businessPlanId = ea.businessPlanId
               and er.expertId = ea.expertId
            where ea.businessPlanId = :businessPlanId
            order by ea.createdAt desc
            """)
    List<BackofficeExpertApplicationLookupResult> findByBusinessPlanId(
            @Param("businessPlanId") Long businessPlanId
    );
}
