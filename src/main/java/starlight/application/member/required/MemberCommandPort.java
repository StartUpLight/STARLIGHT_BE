package starlight.application.member.required;

import starlight.domain.member.entity.Member;

public interface MemberCommandPort {

    Member save(Member member);
}
