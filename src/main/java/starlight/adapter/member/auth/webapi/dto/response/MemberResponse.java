package starlight.adapter.member.auth.webapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

public record MemberResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "starLight@gmail.com")
        String email,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "회원 타입", example = "FOUNDER | EXPERT")
        MemberType memberType
) {
    public static MemberResponse of(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getMemberType()
        );
    }
}
