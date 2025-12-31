package starlight.application.member.auth.provided.dto;

public record AuthTokenResult(
        String accessToken,
        String refreshToken
) {
    public static AuthTokenResult of(String accessToken, String refreshToken) {
        return new AuthTokenResult(accessToken, refreshToken);
    }
}
