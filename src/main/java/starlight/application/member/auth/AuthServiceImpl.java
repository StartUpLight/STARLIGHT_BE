package starlight.application.member.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.member.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.member.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.member.auth.webapi.dto.response.MemberResponse;
import starlight.application.member.auth.provided.AuthUseCase;
import starlight.application.member.auth.required.KeyValueMap;
import starlight.application.member.auth.required.TokenProvider;
import starlight.application.member.provided.CredentialService;
import starlight.application.member.provided.MemberService;
import starlight.domain.member.auth.exception.AuthErrorType;
import starlight.domain.member.auth.exception.AuthException;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final MemberService memberService;
    private final CredentialService credentialService;
    private final TokenProvider tokenProvider;
    private final KeyValueMap redisClient;

    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshTokenExpirationTime;

    /**
     * 회원가입 메서드
     *
     * @param authRequest
     * @return MemberResponse
     */
    @Override
    @Transactional
    public MemberResponse signUp(AuthRequest authRequest) {
        Credential credential = credentialService.createCredential(authRequest.password());
        Member member = memberService.createUser(
                credential,
                authRequest.name(),
                authRequest.email(),
                authRequest.phoneNumber()
        );

        return MemberResponse.of(member);
    }

    /**
     * 로그인 메서드
     *
     * @param signInRequest
     * @return TokenResponse
     */
    @Override
    @Transactional
    public TokenResponse signIn(SignInRequest signInRequest) {
        Member member = memberService.getUserByEmail(signInRequest.email());
        credentialService.checkPassword(member, signInRequest.password());

        TokenResponse tokenResponse = tokenProvider.issueTokens(member);
        redisClient.setValue(member.getEmail(), tokenResponse.refreshToken(), refreshTokenExpirationTime);

        return tokenResponse;
    }

    /**
     * 로그아웃 메서드
     *
     * @param refreshToken
     * @param accessToken
     */
    @Override
    @Transactional
    public void signOut(String refreshToken, String accessToken) {
        if (refreshToken == null || accessToken == null) {
            throw new AuthException(AuthErrorType.TOKEN_NOT_FOUND);
        }
        if (!tokenProvider.validateToken(accessToken)) {
            throw new AuthException(AuthErrorType.TOKEN_INVALID);
        }
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorType.TOKEN_INVALID);
        }
        tokenProvider.logoutTokens(refreshToken, accessToken);
    }

    /**
     * 토큰 재발급 메서드
     *
     * @param token
     * @param member
     * @return tokenResponse
     */
    @Override
    public TokenResponse reissue(String token, Member member) {
        if (token == null) {
            throw new AuthException(AuthErrorType.TOKEN_NOT_FOUND);
        }
        if (member == null) {
            throw new MemberException(MemberErrorType.MEMBER_NOT_FOUND);
        }

        String refreshToken = extractToken(token);
        boolean isValid = tokenProvider.validateToken(refreshToken);

        if (!isValid) {
            throw new AuthException(AuthErrorType.TOKEN_INVALID);
        }

        String email = tokenProvider.getEmail(refreshToken);
        if (!email.equals(member.getEmail())) {
            throw new AuthException(AuthErrorType.TOKEN_INVALID);
        }
        String redisRefreshToken = redisClient.getValue(email);

        if (refreshToken.isEmpty() || redisRefreshToken == null || redisRefreshToken.isEmpty()
                || !redisRefreshToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorType.TOKEN_NOT_FOUND);
        }

        return tokenProvider.reissueTokens(member, refreshToken);
    }

    private String extractToken(String token) {
        String trimmed = token.trim();
        if (trimmed.startsWith("Bearer ")) {
            String rawToken = trimmed.substring(7).trim();
            if (rawToken.isEmpty()) {
                throw new AuthException(AuthErrorType.TOKEN_INVALID);
            }
            return rawToken;
        }
        if (trimmed.isEmpty()) {
            throw new AuthException(AuthErrorType.TOKEN_INVALID);
        }
        return trimmed;
    }
}
