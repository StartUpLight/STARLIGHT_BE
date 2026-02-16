package starlight.adapter.member.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.member.entity.Member;

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
}
