package starlight.application.member.auth.provided.dto;

import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

public record AuthMemberResult(
        Long id,
        String email,
        String phoneNumber,
        MemberType memberType
) {
    public static AuthMemberResult from(Member member) {
        return new AuthMemberResult(
                member.getId(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getMemberType()
        );
    }
}
