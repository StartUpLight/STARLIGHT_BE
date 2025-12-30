package starlight.adapter.member.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.member.required.MemberCommandPort;
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberJpa implements MemberQueryPort, MemberCommandPort {

    private final MemberRepository memberRepository;

    @Override
    public Member getMemberOrThrow(Long id) {
        return memberRepository.findById(id).orElseThrow(
                () -> new MemberException(MemberErrorType.MEMBER_NOT_FOUND)
        );
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }
}
