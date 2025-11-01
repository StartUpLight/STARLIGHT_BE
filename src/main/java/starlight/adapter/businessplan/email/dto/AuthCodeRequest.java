package starlight.adapter.businessplan.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthCodeRequest(
    @Email
    @NotBlank
    @Schema(description = "인증코드를 보낼 이메일 주소", example = "kjeng7897@gmail.com")
    String email
){};
