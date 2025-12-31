package starlight.shared.auth;

import starlight.domain.member.entity.Member;

public interface AuthenticatedMember {

    Long getMemberId();

    Member getUser();
}
