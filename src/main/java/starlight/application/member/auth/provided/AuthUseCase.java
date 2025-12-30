package starlight.application.member.auth.provided;

import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.member.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.member.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.member.auth.webapi.dto.response.MemberResponse;
import starlight.domain.member.entity.Member;

public interface AuthUseCase {

    MemberResponse signUp(AuthRequest authRequest);

    TokenResponse signIn(SignInRequest signInRequest);

    void signOut(String refreshToken, String accessToken);

    TokenResponse reissue(String token, Member member);
}
