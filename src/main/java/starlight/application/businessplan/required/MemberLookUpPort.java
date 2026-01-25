package starlight.application.businessplan.required;

import starlight.domain.member.entity.Member;

public interface MemberLookUpPort {
    Member findByIdOrThrow(Long id);
}
