package starlight.application.member.required;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderId(String provider, String providerId);
}
