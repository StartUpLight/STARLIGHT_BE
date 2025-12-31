package starlight.application.member.auth.provided;

import starlight.application.member.auth.provided.dto.AuthMemberResult;
import starlight.application.member.auth.provided.dto.AuthTokenResult;
import starlight.application.member.auth.provided.dto.SignInCommand;
import starlight.application.member.auth.provided.dto.SignUpCommand;
import starlight.domain.member.entity.Member;

public interface AuthUseCase {

    AuthMemberResult signUp(SignUpCommand command);

    AuthTokenResult signIn(SignInCommand command);

    void signOut(String refreshToken, String accessToken);

    AuthTokenResult reissue(String token, Member member);
}
