package starlight.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.application.member.provided.MemberService;
import starlight.application.member.required.MemberRepository;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    /**
     * Credential을 생성하고 저장하는 메서드
     * @param credential
     * @param authRequest
     * @return Member
     */
    public Member createUser(Credential credential, AuthRequest authRequest) {
        memberRepository.findByEmail(authRequest.email()).ifPresent(existingUser -> {
            throw new MemberException(MemberErrorType.MEMBER_ALREADY_EXISTS);
        });
        Member member = authRequest.toMember(credential);
        return memberRepository.save(member);
    }

    /**
     * 이메일로 사용자를 조회하는 메서드
     * @param email
     * @return Member
     */
    public Member getUserByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorType.MEMBER_NOT_FOUND));
    }
}

