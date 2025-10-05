package starlight.adapter.auth.webapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

public record AuthRequest(

        @Schema(description = "이메일", example = "starLight@gmail.com")
        String email,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "비밀번호", example = "password123")
        String password
) {
    public Member toMember(Credential credential) {
        return Member.of(null, email, phoneNumber, MemberType.WRITER, credential);
    }
}