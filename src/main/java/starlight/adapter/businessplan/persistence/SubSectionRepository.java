package starlight.adapter.businessplan.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.Optional;

public interface SubSectionRepository extends JpaRepository<SubSection, Long> {

    // Overview 관련 - SubSection ID만 조회 후 직접 조회
    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT o.overview_basic_id FROM overview o WHERE o.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findOverviewBasicByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    // Problem Recognition 관련 - SubSection ID만 조회 후 직접 조회
    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT pr.problem_background_id FROM problem_recognition pr WHERE pr.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findProblemBackgroundByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT pr.problem_purpose_id FROM problem_recognition pr WHERE pr.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findProblemPurposeByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT pr.problem_market_id FROM problem_recognition pr WHERE pr.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findProblemMarketByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    // Feasibility 관련 - SubSection ID만 조회 후 직접 조회
    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT f.feasibility_strategy_id FROM feasibility f WHERE f.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findFeasibilityStrategyByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT f.feasibility_market_id FROM feasibility f WHERE f.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findFeasibilityMarketByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    // Growth Strategy 관련 - SubSection ID만 조회 후 직접 조회
    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT gt.growth_model_id FROM growth_tactic gt WHERE gt.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findGrowthModelByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT gt.growth_funding_id FROM growth_tactic gt WHERE gt.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findGrowthFundingByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT gt.growth_entry_id FROM growth_tactic gt WHERE gt.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findGrowthEntryByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    // Team Competence 관련 - SubSection ID만 조회 후 직접 조회
    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT tc.team_founder_id FROM team_competence tc WHERE tc.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findTeamFounderByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    @Query(value = """
            SELECT s.* FROM sub_section s
            WHERE s.id = (SELECT tc.team_members_id FROM team_competence tc WHERE tc.id = :businessPlanId)
            """, nativeQuery = true)
    Optional<SubSection> findTeamMembersByBusinessPlanId(@Param("businessPlanId") Long businessPlanId);

    // 통합 메서드
    default Optional<SubSection> findByBusinessPlanIdAndSubSectionName(Long businessPlanId,
            SubSectionName subSectionName) {
        return switch (subSectionName) {
            case OVERVIEW_BASIC -> findOverviewBasicByBusinessPlanId(businessPlanId);
            case PROBLEM_BACKGROUND -> findProblemBackgroundByBusinessPlanId(businessPlanId);
            case PROBLEM_PURPOSE -> findProblemPurposeByBusinessPlanId(businessPlanId);
            case PROBLEM_MARKET -> findProblemMarketByBusinessPlanId(businessPlanId);
            case FEASIBILITY_STRATEGY -> findFeasibilityStrategyByBusinessPlanId(businessPlanId);
            case FEASIBILITY_MARKET -> findFeasibilityMarketByBusinessPlanId(businessPlanId);
            case GROWTH_MODEL -> findGrowthModelByBusinessPlanId(businessPlanId);
            case GROWTH_FUNDING -> findGrowthFundingByBusinessPlanId(businessPlanId);
            case GROWTH_ENTRY -> findGrowthEntryByBusinessPlanId(businessPlanId);
            case TEAM_FOUNDER -> findTeamFounderByBusinessPlanId(businessPlanId);
            case TEAM_MEMBERS -> findTeamMembersByBusinessPlanId(businessPlanId);
        };
    }
}
