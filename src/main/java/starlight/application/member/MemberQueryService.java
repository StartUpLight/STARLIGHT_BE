package starlight.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.application.member.provided.MemberQueryUseCase;
import starlight.application.member.required.MemberCommandPort;
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

@Service
@RequiredArgsConstructor
public class MemberQueryService implements MemberQueryUseCase {

    private final MemberQueryPort memberQueryPort;
    private final MemberCommandPort memberCommandPort;

    /**
     * Credential을 생성하고 저장하는 메서드
     * @param credential
     * @param authRequest
     * @return Member
     */
    public Member createUser(Credential credential, String name, String email, String phoneNumber) {
        memberQueryPort.findByEmail(email).ifPresent(existingUser -> {
            throw new MemberException(MemberErrorType.MEMBER_ALREADY_EXISTS);
        });
        Member member = Member.create(name, email, phoneNumber, MemberType.FOUNDER, credential, null);
        return memberCommandPort.save(member);
    }

    /**
     * 이메일로 사용자를 조회하는 메서드
     * @param email
     * @return Member
     */
    public Member getUserByEmail(String email) {
        return memberQueryPort.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorType.MEMBER_NOT_FOUND));
    }

    @Override
    public Member getUserById(Long id) {
        return memberQueryPort.findByIdOrThrow(id);
    }
}
