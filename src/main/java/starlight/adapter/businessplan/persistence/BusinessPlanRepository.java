package starlight.adapter.businessplan.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.Optional;

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
}
