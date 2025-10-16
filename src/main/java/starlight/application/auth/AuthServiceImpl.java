package starlight.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.auth.webapi.dto.response.MemberResponse;
import starlight.application.auth.provided.AuthService;
import starlight.application.auth.required.KeyValueMap;
import starlight.application.auth.required.TokenProvider;
import starlight.application.member.provided.CredentialService;
import starlight.application.member.provided.MemberService;
import starlight.domain.auth.exception.AuthErrorType;
import starlight.domain.auth.exception.AuthException;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberService memberService;
    private final CredentialService credentialService;
    private final TokenProvider tokenProvider;
    private final KeyValueMap redisClient;

    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshTokenExpirationTime;

    /**
     * 회원가입 메서드
     * @param authRequest
     * @return MemberResponse
     */
    @Override
    @Transactional
    public MemberResponse signUp(AuthRequest authRequest) {
        Credential credential = credentialService.createCredential(authRequest);
        Member member = memberService.createUser(credential, authRequest);

        return MemberResponse.of(member);
    }

    /**
     * 로그인 메서드
     * @param signInRequest
     * @return TokenResponse
     */
    @Override
    @Transactional
    public TokenResponse signIn(SignInRequest signInRequest) {
        Member member = memberService.getUserByEmail(signInRequest.email());
        credentialService.checkPassword(member, signInRequest.password());

        TokenResponse tokenResponse = tokenProvider.createToken(member);
        redisClient.setValue(member.getEmail(), tokenResponse.refreshToken(), refreshTokenExpirationTime);

        return tokenResponse;
    }

    /**
     * 로그아웃 메서드
     * @param refreshToken
     * @param accessToken
     */
    @Override
    @Transactional
    public void signOut(String refreshToken, String accessToken) {
        if(refreshToken==null || accessToken==null) throw new AuthException(AuthErrorType.TOKEN_NOT_FOUND);
        if(!tokenProvider.validateToken(accessToken)) {
            throw new AuthException(AuthErrorType.TOKEN_INVALID);
        }
        tokenProvider.invalidateTokens(refreshToken, accessToken);
    }

    /**
     * 토큰 재발급 메서드
     * @param token
     * @param member
     * @return tokenResponse
     */
    @Override
    public TokenResponse recreate(String token, Member member) {
        if (token ==null) {
            throw new AuthException(AuthErrorType.TOKEN_NOT_FOUND);
        }
        if (member == null) {
            throw new MemberException(MemberErrorType.MEMBER_NOT_FOUND);
        }

        String refreshToken = token.substring(7);
        boolean isValid = tokenProvider.validateToken(refreshToken);

        if (!isValid) {
            throw new AuthException(AuthErrorType.TOKEN_INVALID);
        }

        String email = tokenProvider.getEmail(refreshToken);
        String redisRefreshToken = redisClient.getValue(email);

        if (refreshToken.isEmpty() || redisRefreshToken.isEmpty() || !redisRefreshToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorType.TOKEN_NOT_FOUND);
        }

        return tokenProvider.recreate(member, refreshToken);
    }
}

