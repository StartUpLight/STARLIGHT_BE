package starlight.application.member.auth.required;

import jakarta.servlet.http.HttpServletRequest;
import starlight.application.member.auth.provided.dto.AuthTokenResult;
import starlight.domain.member.entity.Member;

public interface TokenProvider {

    String createAccessToken(Member member);

    AuthTokenResult issueTokens(Member member);

    AuthTokenResult reissueTokens(Member member, String refreshToken);

    boolean validateToken(String token);

    String getEmail(String token);

    Long getExpirationTime(String token);

    /**
     * @deprecated Authorization 헤더 파싱은 {@code AuthTokenResolver}를 사용하세요.
     *             1.4.0부터 Deprecated 처리되었고, 추후 제거될 수 있습니다.
     */
    @Deprecated(since = "1.4.0", forRemoval = false)
    String resolveRefreshToken(HttpServletRequest request);

    /**
     * @deprecated Authorization 헤더 파싱은 {@code AuthTokenResolver}를 사용하세요.
     *             1.4.0부터 Deprecated 처리되었고, 추후 제거될 수 있습니다.
     */
    @Deprecated(since = "1.4.0", forRemoval = false)
    String resolveAccessToken(HttpServletRequest request);

    void logoutTokens(String refreshToken, String accessToken);
}
