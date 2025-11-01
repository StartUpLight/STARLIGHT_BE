package starlight.adapter.businessplan.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CertificateRequest(
    @Email
    @NotBlank
    @Schema(description = "검증할 이메일 주소", example = "kjeng7897@gmail.com")
    String email,

    @NotBlank
    @Schema(description = "인증 코드", example = "123456")
    String authCode
){};
