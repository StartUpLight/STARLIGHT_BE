package starlight.adapter.auth.webapi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

public record AuthRequest(

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이어야 합니다")
        @Schema(description = "이메일", example = "starLight@gmail.com")
        String email,

        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$", message = "전화번호 형식이 올바르지 않습니다")
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "비밀번호", example = "password123")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {
    public Member toMember(Credential credential) {
        return Member.create(null, email, phoneNumber, MemberType.FOUNDER, credential);
    }
}