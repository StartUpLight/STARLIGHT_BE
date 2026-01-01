package starlight.adapter.member.webapi.dto;

import starlight.domain.member.entity.Member;

public record MemberDetailResponse (
    Long id,

    String name,

    String email,

    String phoneNumber,

    String provider,

    String profileImageUrl
){
    public static MemberDetailResponse fromMember(Member member) {
        return new MemberDetailResponse(
            member.getId(),
            member.getName(),
            member.getEmail(),
            member.getPhoneNumber(),
            member.getProvider(),
            member.getProfileImageUrl()
        );
    }
}
