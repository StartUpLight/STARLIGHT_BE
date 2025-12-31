package starlight.application.member.auth.provided;

import starlight.application.member.auth.provided.dto.AuthMemberResult;
import starlight.application.member.auth.provided.dto.AuthTokenResult;
import starlight.application.member.auth.provided.dto.SignInInput;
import starlight.application.member.auth.provided.dto.SignUpInput;

public interface AuthUseCase {

    AuthMemberResult signUp(SignUpInput input);

    AuthTokenResult signIn(SignInInput input);

    void signOut(String refreshToken, String accessToken);

    AuthTokenResult reissue(String token, Long memberId);
}
