package starlight.adapter.auth.webapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignInRequest(

        @Schema(description = "이메일", example = "starLight@gmail.com")
        String email,

        @Schema(description = "비밀번호", example = "password123")
        String password
) { }