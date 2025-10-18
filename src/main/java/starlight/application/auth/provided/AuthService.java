package starlight.application.auth.provided;

import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.auth.webapi.dto.response.MemberResponse;
import starlight.domain.member.entity.Member;

public interface AuthService {

    MemberResponse signUp(AuthRequest authRequest);

    TokenResponse signIn(SignInRequest signInRequest);

    void signOut(String refreshToken, String accessToken);

    TokenResponse recreate(String token, Member member);
}

