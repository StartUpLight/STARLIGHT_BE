package starlight.application.auth.required;

import jakarta.servlet.http.HttpServletRequest;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.domain.member.entity.Member;

public interface TokenProvider {

    String createAccessToken(Member member);

    TokenResponse createToken(Member member);

    TokenResponse recreate(Member member, String refreshToken);

    boolean validateToken(String token);

    String getEmail(String token);

    Long getExpirationTime(String token);

    String resolveRefreshToken(HttpServletRequest request);

    String resolveAccessToken(HttpServletRequest request);

    void invalidateTokens(String refreshToken, String accessToken);
}
