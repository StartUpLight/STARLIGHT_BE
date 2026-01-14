package starlight.application.member.provided;

import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

public interface MemberUseCase {

    Member createUser(Credential credential, String name, String email, String phoneNumber);

    Member getUserByEmail(String email);

    Member getUserById(Long id);
}
