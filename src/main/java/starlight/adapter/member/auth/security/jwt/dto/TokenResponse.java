package starlight.adapter.member.auth.security.jwt.dto;

import starlight.application.member.auth.provided.dto.AuthTokenResult;

public record TokenResponse(
        String accessToken,

        String refreshToken
) {
    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }

    public static TokenResponse from(AuthTokenResult result) {
        return new TokenResponse(result.accessToken(), result.refreshToken());
    }
}
