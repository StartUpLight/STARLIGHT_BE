package starlight.adapter.member.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import starlight.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderId(String provider, String providerId);

    List<Member> findByIdIn(Collection<Long> ids);

    @Query("""
            select m.id
            from Member m
            where lower(m.name) like lower(concat('%', :keyword, '%'))
               or lower(m.email) like lower(concat('%', :keyword, '%'))
            """)
    List<Long> findIdsByNameOrEmailKeyword(@Param("keyword") String keyword);

    @Query("""
            select m
            from Member m
            where (
                :keyword is null
                or lower(m.name) like lower(concat('%', :keyword, '%'))
                or lower(m.email) like lower(concat('%', :keyword, '%'))
            )
            """)
    Page<Member> findBackofficeUserPage(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select count(m.id)
            from Member m
            """)
    long countUsers();

    @Query("""
            select count(m.id)
            from Member m
            where coalesce(m.modifiedAt, m.createdAt) >= :since
            """)
    long countActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("""
            select count(m.id)
            from Member m
            where m.createdAt >= :fromInclusive
              and m.createdAt < :toExclusive
            """)
    long countNewUsersBetween(
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive
    );

    @Query("""
            select count(m.id)
            from Member m
            where m.isDeleted = true
              and coalesce(m.modifiedAt, m.createdAt) >= :fromInclusive
              and coalesce(m.modifiedAt, m.createdAt) < :toExclusive
            """)
    long countChurnedUsersBetween(
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive
    );

    @Query("""
            select m
            from Member m
            where m.createdAt >= :fromInclusive
              and m.createdAt < :toExclusive
            """)
    List<Member> findSignupsBetween(
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive
    );
}
