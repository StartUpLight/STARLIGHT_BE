package starlight.adapter.backoffice.businessplan.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanMemberLookupResult;
import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.Collection;
import java.util.List;

public interface BackofficeUserBusinessPlanRepository extends JpaRepository<BusinessPlan, Long> {

    @Query("""
            select new starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanMemberLookupResult(
                bp.memberId,
                bp.id
            )
            from BusinessPlan bp
            where bp.memberId in :userIds
            """)
    List<BackofficeUserBusinessPlanMemberLookupResult> findBusinessPlansByUserIds(
            @Param("userIds") Collection<Long> userIds
    );

    @Query(
            value = """
                    select new starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanLookupResult(
                        bp.id,
                        bp.title,
                        bp.planStatus,
                        coalesce(bp.modifiedAt, bp.createdAt)
                    )
                    from BusinessPlan bp
                    where bp.memberId = :userId
                      and (
                            :scored is null
                            or (
                                :scored = true
                                and exists (
                                    select 1
                                    from AiReport ar
                                    where ar.businessPlanId = bp.id
                                )
                            )
                            or (
                                :scored = false
                                and not exists (
                                    select 1
                                    from AiReport ar
                                    where ar.businessPlanId = bp.id
                                )
                            )
                      )
                    """,
            countQuery = """
                    select count(bp.id)
                    from BusinessPlan bp
                    where bp.memberId = :userId
                      and (
                            :scored is null
                            or (
                                :scored = true
                                and exists (
                                    select 1
                                    from AiReport ar
                                    where ar.businessPlanId = bp.id
                                )
                            )
                            or (
                                :scored = false
                                and not exists (
                                    select 1
                                    from AiReport ar
                                    where ar.businessPlanId = bp.id
                                )
                            )
                      )
                    """
    )
    Page<BackofficeUserBusinessPlanLookupResult> findUserBusinessPlanPage(
            @Param("userId") Long userId,
            @Param("scored") Boolean scored,
            Pageable pageable
    );
}
