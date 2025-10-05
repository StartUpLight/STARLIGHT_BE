package starlight.application.auth.required;

import jakarta.servlet.http.HttpServletRequest;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.domain.member.entity.Member;

public interface TokenProvider {

    /**
     * AccessToken을 생성하는 메서드
     * @param member
     * @return String
     */
    String createAccessToken(Member member);

    /**
     * AccessToken과 RefreshToken을 생성하는 메서드
     * @param member
     * @return TokenResponse
     */
    TokenResponse createToken(Member member);

    /**
     * AccessToken과 RefreshToken을 재발급하는 메서드
     * @param member
     * @param refreshToken
     * @return TokenResponse
     */
    TokenResponse recreate(Member member, String refreshToken);

    /**
     * 토큰 유효성 검사 메서드
     * @param token
     * @return boolean
     */
    boolean validateToken(String token);

    /**
     * Bearer Token에서 이메일을 추출하는 메서드
     * @param token
     * @return String
     */
    String getEmail(String token);

    /**
     * AccessToken의 만료 시간을 가져오는 메서드
     * @param token
     * @return Long
     */
    Long getExpirationTime(String token);

    /**
     * Bearer Token에서 RefreshToken을 추출하는 메서드
     * @param request
     * @return String
     */
    String resolveRefreshToken(HttpServletRequest request);

    /**
     * Bearer Token에서 AccessToken을 추출하는 메서드
     * @param request
     * @return String
     */
    String resolveAccessToken(HttpServletRequest request);

    /**
     * 토큰을 무효화하는 메서드
     * @param refreshToken
     * @param accessToken
     */
    void invalidateTokens(String refreshToken, String accessToken);
}
