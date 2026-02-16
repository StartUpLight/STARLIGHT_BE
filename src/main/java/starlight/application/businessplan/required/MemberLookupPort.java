package starlight.application.businessplan.required;

import starlight.domain.member.entity.Member;

public interface MemberLookupPort {
    Member findByIdOrThrow(Long id);
}
