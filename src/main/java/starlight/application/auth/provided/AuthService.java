package starlight.application.auth.provided;

import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.auth.webapi.dto.response.MemberResponse;
import starlight.domain.member.entity.Member;

public interface AuthService {

    /**
     * 회원가입 메서드
     * @param authRequest
     * @return MemberResponse
     */
    MemberResponse signUp(AuthRequest authRequest);

    /**
     * 로그인 메서드
     * @param signInRequest
     * @return TokenResponse
     */
    TokenResponse signIn(SignInRequest signInRequest);

    /**
     * 로그아웃 메서드
     * @param refreshToken
     * @param accessToken
     */
    void signOut(String refreshToken, String accessToken);

    /**
     * 토큰 재발급 메서드
     * @param token
     * @param member
     * @return tokenResponse
     */
    TokenResponse recreate(String token, Member member);
}

