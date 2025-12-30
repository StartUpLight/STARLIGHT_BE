package starlight.adapter.member.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.application.member.auth.required.KeyValueMap;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;
import starlight.shared.apiPayload.exception.GlobalException;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private KeyValueMap redisClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private Member member;
    private Key key;
    private final String secretKey = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1hbmQtdmFsaWRhdGlvbg==";
    private final long accessTokenExpirationTime = 3600000L;    // 1시간
    private final long refreshTokenExpirationTime = 86400000L;  // 24시간

    @BeforeEach
    void setUp() {
        member = Member.create("정성호","test@example.com", "010-2112-9765", MemberType.FOUNDER, null, "1234.png");

        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(secretKeyBytes);

        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpirationTime", accessTokenExpirationTime);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationTime", refreshTokenExpirationTime);
        ReflectionTestUtils.setField(jwtTokenProvider, "key", key);
    }

    @Test
    @DisplayName("AccessToken 생성 성공")
    void createAccessToken_Success() {
        // when
        String accessToken = jwtTokenProvider.createAccessToken(member);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(jwtTokenProvider.getEmail(accessToken)).isEqualTo(member.getEmail());
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
    }

    @Test
    @DisplayName("AccessToken과 RefreshToken 생성 성공")
    void issueTokens_Success() {
        // when
        TokenResponse tokenResponse = jwtTokenProvider.issueTokens(member);

        // then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isNotNull();
        assertThat(tokenResponse.refreshToken()).isNotNull();
        assertThat(jwtTokenProvider.validateToken(tokenResponse.accessToken())).isTrue();
        assertThat(jwtTokenProvider.validateToken(tokenResponse.refreshToken())).isTrue();
    }

    @Test
    @DisplayName("토큰 재발급 성공 - RefreshToken 유효기간이 충분한 경우")
    void recreate_Success_WithValidRefreshToken() {
        // given
        TokenResponse originalToken = jwtTokenProvider.issueTokens(member);

        // when
        TokenResponse newToken = jwtTokenProvider.reissueTokens(member, originalToken.refreshToken());

        // then
        assertThat(newToken).isNotNull();
        assertThat(newToken.accessToken()).isNotNull();
        assertThat(jwtTokenProvider.validateToken(newToken.accessToken())).isTrue();
        assertThat(newToken.refreshToken()).isEqualTo(originalToken.refreshToken());
        assertThat(jwtTokenProvider.getEmail(newToken.accessToken())).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("토큰 재발급 성공 - RefreshToken 재발급 필요한 경우")
    void recreate_Success_WithExpiredRefreshToken() {
        // given
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        Date now = new Date();
        String expiredRefreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 1000L)) // 1초
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // when
        TokenResponse newToken = jwtTokenProvider.reissueTokens(member, expiredRefreshToken);

        // then
        assertThat(newToken).isNotNull();
        assertThat(newToken.accessToken()).isNotNull();
        assertThat(newToken.refreshToken()).isNotEqualTo(expiredRefreshToken);
    }

    @Test
    @DisplayName("토큰 유효성 검사 성공")
    void validateToken_Success() {
        // given
        String accessToken = jwtTokenProvider.createAccessToken(member);

        // when
        boolean isValid = jwtTokenProvider.validateToken(accessToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 만료된 토큰")
    void validateToken_Fail_ExpiredToken() {
        // given
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        Date now = new Date();
        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now.getTime() - 2000L))
                .setExpiration(new Date(now.getTime() - 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 잘못된 토큰")
    void validateToken_Fail_InvalidToken() {
        // given
        String invalidToken = "invalid.token.string";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void getEmail_Success() {
        // given
        String accessToken = jwtTokenProvider.createAccessToken(member);

        // when
        String email = jwtTokenProvider.getEmail(accessToken);

        // then
        assertThat(email).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("토큰 만료 시간 가져오기 성공")
    void getExpirationTime_Success() {
        // given
        String accessToken = jwtTokenProvider.createAccessToken(member);

        // when
        Long expirationTime = jwtTokenProvider.getExpirationTime(accessToken);

        // then
        assertThat(expirationTime).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("RefreshToken 추출 성공")
    void resolveRefreshToken_Success() {
        // given
        String token = "testToken";
        given(request.getHeader("Authorization")).willReturn("Bearer " + token);

        // when
        String resolvedToken = jwtTokenProvider.resolveRefreshToken(request);

        // then
        assertThat(resolvedToken).isEqualTo(token);
    }

    @Test
    @DisplayName("RefreshToken 추출 실패 - Authorization 헤더 없음")
    void resolveRefreshToken_Fail_NoHeader() {
        // given
        given(request.getHeader("Authorization")).willReturn(null);

        // when
        String resolvedToken = jwtTokenProvider.resolveRefreshToken(request);

        // then
        assertThat(resolvedToken).isNull();
    }

    @Test
    @DisplayName("AccessToken 추출 성공")
    void resolveAccessToken_Success() {
        // given
        String token = "testToken";
        given(request.getHeader("Authorization")).willReturn("Bearer " + token);

        // when
        String resolvedToken = jwtTokenProvider.resolveAccessToken(request);

        // then
        assertThat(resolvedToken).isEqualTo(token);
    }

    @Test
    @DisplayName("토큰 무효화 성공")
    void logoutTokens_Success() {
        // given
        TokenResponse tokenResponse = jwtTokenProvider.issueTokens(member);

        // when
        jwtTokenProvider.logoutTokens(tokenResponse.refreshToken(), tokenResponse.accessToken());

        // then
        verify(redisClient).deleteValue(eq(member.getEmail()));
        verify(redisClient).setValue(eq(tokenResponse.accessToken()), eq("logout"), anyLong());
    }

    @Test
    @DisplayName("토큰 무효화 실패 - 유효하지 않은 RefreshToken")
    void logoutTokens_Fail_InvalidRefreshToken() {
        // given
        String invalidRefreshToken = "invalid.refresh.token";
        String accessToken = jwtTokenProvider.createAccessToken(member);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.logoutTokens(invalidRefreshToken, accessToken))
                .isInstanceOf(GlobalException.class);
    }
}
