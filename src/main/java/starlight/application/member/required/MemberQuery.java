package starlight.application.member.required;

import starlight.domain.member.entity.Member;

public interface MemberQuery {

    Member getOrThrow(Long id);
}
