package starlight.application.member.provided;

import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

public interface MemberService {

    Member createUser(Credential credential, AuthRequest authRequest);

    Member getUserByEmail(String email);
}
