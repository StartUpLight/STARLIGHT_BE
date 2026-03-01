package starlight.adapter.businessplan.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import starlight.domain.businessplan.enumerate.PlanStatus;

public interface BusinessPlanRepository extends JpaRepository<BusinessPlan, Long> {

    @EntityGraph(attributePaths = { "feasibility", "problemRecognition", "growthTactic", "teamCompetence", "overview" })
    Optional<BusinessPlan> findById(Long id);

    @Query("""
            SELECT bp
            FROM BusinessPlan bp
            WHERE bp.memberId = :memberId
            ORDER BY COALESCE(bp.modifiedAt, bp.createdAt) DESC, bp.id DESC
            """)
    Page<BusinessPlan> findAllByMemberIdOrderedByLastSavedAt(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
            SELECT bp
            FROM BusinessPlan bp
            WHERE bp.memberId = :memberId
            ORDER BY COALESCE(bp.modifiedAt, bp.createdAt) DESC, bp.id DESC
            """)
    List<BusinessPlan> findAllByMemberIdOrderByLastSavedAt(@Param("memberId") Long memberId);

    @Query("""
            SELECT DISTINCT bp
            FROM BusinessPlan bp
            LEFT JOIN FETCH bp.overview o
            LEFT JOIN FETCH o.overviewBasic
            LEFT JOIN FETCH bp.problemRecognition pr
            LEFT JOIN FETCH pr.problemBackground
            LEFT JOIN FETCH pr.problemPurpose
            LEFT JOIN FETCH pr.problemMarket
            LEFT JOIN FETCH bp.feasibility f
            LEFT JOIN FETCH f.feasibilityStrategy
            LEFT JOIN FETCH f.feasibilityMarket
            LEFT JOIN FETCH bp.growthTactic gt
            LEFT JOIN FETCH gt.growthModel
            LEFT JOIN FETCH gt.growthFunding
            LEFT JOIN FETCH gt.growthEntry
            LEFT JOIN FETCH bp.teamCompetence tc
            LEFT JOIN FETCH tc.teamFounder
            LEFT JOIN FETCH tc.teamMembers
            WHERE bp.id = :id
            """)
    Optional<BusinessPlan> findByIdWithAllSubSections(@Param("id") Long id);

    @Query("""
            select bp
            from BusinessPlan bp
            where (:status is null or bp.planStatus = :status)
              and (
                  :keyword is null
                  or lower(bp.title) like lower(concat('%', :keyword, '%'))
                  or bp.memberId in :memberIds
              )
            """)
    Page<BusinessPlan> findBackofficePage(
            @Param("status") PlanStatus status,
            @Param("keyword") String keyword,
            @Param("memberIds") List<Long> memberIds,
            Pageable pageable
    );

    @Query("""
            select bp
            from BusinessPlan bp
            where (:status is null or bp.planStatus = :status)
              and (
                  :keyword is null
                  or lower(bp.title) like lower(concat('%', :keyword, '%'))
                  or bp.memberId in :memberIds
              )
              and (:from is null or coalesce(bp.modifiedAt, bp.createdAt) >= :from)
              and (:to is null or coalesce(bp.modifiedAt, bp.createdAt) < :to)
            """)
    List<BusinessPlan> findBackofficeAllForDashboard(
            @Param("status") PlanStatus status,
            @Param("keyword") String keyword,
            @Param("memberIds") List<Long> memberIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
