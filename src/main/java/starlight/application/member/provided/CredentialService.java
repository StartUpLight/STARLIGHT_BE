package starlight.application.member.provided;

import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

public interface CredentialService {

    /**
     * Credential을 생성하고 저장하는 메서드
     * @param authRequest
     * @return Credential
     */
    Credential createCredential(AuthRequest authRequest);

    /**
     * 비밀번호를 확인하는 메서드
     * @param member
     * @param password
     */
    void checkPassword(Member member, String password);
}
