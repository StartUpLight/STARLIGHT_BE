package starlight.adapter.expertReport.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expertReport.entity.ExpertReport;

import java.util.List;
import java.util.Optional;

public interface ExpertReportRepository extends JpaRepository<ExpertReport, Long> {

    boolean existsByToken(String token);

    @EntityGraph(attributePaths = {"comments"})
    @Query("select er from ExpertReport er where er.token = :token")
    Optional<ExpertReport> findByTokenWithComments(@Param("token") String token);

    @EntityGraph(attributePaths = {"comments"})
    @Query("select er from ExpertReport er where er.businessPlanId = :businessPlanId order by er.createdAt desc")
    List<ExpertReport> findAllByBusinessPlanIdWithCommentsOrderByCreatedAtDesc(
            @Param("businessPlanId") Long businessPlanId
    );
}
