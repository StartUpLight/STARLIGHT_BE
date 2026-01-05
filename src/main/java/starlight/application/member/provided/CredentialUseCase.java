package starlight.application.member.provided;

import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

public interface CredentialService {

    Credential createCredential(String rawPassword);

    /**
     * 비밀번호를 확인하는 메서드
     * @param member
     * @param password
     */
    void checkPassword(Member member, String password);
}
