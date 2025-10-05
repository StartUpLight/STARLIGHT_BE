package starlight.application.member.provided;

import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

public interface MemberService {

    /**
     * Credential을 생성하고 저장하는 메서드
     * @param credential
     * @param authRequest
     * @return Member
     */
    Member createUser(Credential credential, AuthRequest authRequest);

    /**
     * 이메일로 사용자를 조회하는 메서드
     * @param email
     * @return Member
     */
    Member getUserByEmail(String email);
}
