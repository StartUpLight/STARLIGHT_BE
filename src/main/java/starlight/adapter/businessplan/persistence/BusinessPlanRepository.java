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
}
