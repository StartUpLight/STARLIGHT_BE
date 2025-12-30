package starlight.application.member.required;

import starlight.domain.member.entity.Member;

import java.util.Optional;

public interface MemberQueryPort {

    Member getOrThrow(Long id);

    Optional<Member> findByEmail(String email);
}
