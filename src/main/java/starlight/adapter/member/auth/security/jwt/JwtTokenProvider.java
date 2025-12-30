package starlight.adapter.member.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.application.member.auth.required.KeyValueMap;
import starlight.application.member.auth.required.TokenProvider;
import starlight.domain.member.entity.Member;
import starlight.shared.apiPayload.exception.GlobalErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

    private Key key;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.token.access-expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.token.refresh-expiration-time}")
    private long refreshTokenExpirationTime;

    @Value("${jwt.prefix:Bearer}")
    private String jwtPrefix;

    private final KeyValueMap redisClient;

    @PostConstruct
    protected void init() {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(secretKeyBytes);
    }

    /**
     * AccessToken을 생성하는 메서드
     *
     * @param member
     * @return String AccessToken
     */
    @Override
    public String createAccessToken(Member member) {
        Claims claims = getClaims(member);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * RefreshToken을 생성하는 메서드
     *
     * @param member
     * @return String RefreshToken
     */
    private String createRefreshToken(Member member) {
        Claims claims = getClaims(member);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * AccessToken과 RefreshToken을 생성하는 메서드
     *
     * @param member
     * @return TokenResponse
     */
    @Override
    public TokenResponse createToken(Member member) {
        return TokenResponse.of(
                createAccessToken(member),
                createRefreshToken(member)
        );
    }

    /**
     * AccessToken과 RefreshToken을 재발급하는 메서드
     *
     * @param member
     * @param refreshToken
     * @return TokenResponse
     */
    @Override
    public TokenResponse recreate(Member member, String refreshToken) {
        String accessToken = createAccessToken(member);

        if(getExpirationTime(refreshToken) <= getExpirationTime(accessToken)) {
            refreshToken = createRefreshToken(member);
        }
        return TokenResponse.of(accessToken, refreshToken);
    }

    /**
     * 토큰 유효성 검사 메서드
     *
     * @param token
     * @return boolean
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Bearer Token에서 이메일을 추출하는 메서드
     *
     * @param token
     * @return String
     */
    @Override
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * AccessToken의 만료 시간을 가져오는 메서드
     *
     * @param token
     * @return Long
     */
    @Override
    public Long getExpirationTime(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration().getTime();
    }

    /**
     * Claims 객체를 생성하는 메서드
     *
     * @param member
     * @return Claims
     */
    private Claims getClaims(Member member) {
        return Jwts.claims().setSubject(member.getEmail());
    }


    /**
     * Bearer Token에서 RefreshToken을 추출하는 메서드
     *
     * @deprecated Authorization 헤더 파싱은 {@code AuthTokenResolver}를 사용하세요.
     *             1.4.0부터 Deprecated 처리되었고, 추후 제거될 수 있습니다.
     */
    @Deprecated(since = "1.4.0", forRemoval = false)
    @Override
    public String resolveRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String prefix = normalizePrefix(jwtPrefix);
        if (bearerToken != null && bearerToken.toLowerCase().startsWith(prefix)) {
            return bearerToken.substring(prefix.length()).trim();
        }
        return null;
    }

    /**
     * Bearer Token에서 AccessToken을 추출하는 메서드
     *
     * @deprecated Authorization 헤더 파싱은 {@code AuthTokenResolver}를 사용하세요.
     *             1.4.0부터 Deprecated 처리되었고, 추후 제거될 수 있습니다.
     */
    @Deprecated(since = "1.4.0", forRemoval = false)
    @Override
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String prefix = normalizePrefix(jwtPrefix);
        if (bearerToken != null && bearerToken.toLowerCase().startsWith(prefix)) {
            return bearerToken.substring(prefix.length()).trim();
        }
        return null;
    }

    /**
     * 토큰을 무효화하는 메서드
     *
     * @param refreshToken
     * @param accessToken
     * @throws GlobalException
     */
    @Override
    @Transactional
    public void invalidateTokens(String refreshToken, String accessToken) {
        if (!validateToken(refreshToken)) {
            throw new GlobalException(GlobalErrorType.INVALID_TOKEN);
        }
        redisClient.deleteValue(getEmail(refreshToken));
        redisClient.setValue(accessToken, "logout", getExpirationTime(accessToken));
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "bearer ";
        }
        String normalized = prefix.trim().toLowerCase();
        return normalized.endsWith(" ") ? normalized : normalized + " ";
    }
}
