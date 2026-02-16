package starlight.adapter.member.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.order.required.BackofficeOrderMemberLookupPort;
import starlight.application.backoffice.order.required.dto.BackofficeOrderMemberLookupResult;
import starlight.application.businessplan.required.MemberLookupPort;
import starlight.application.member.required.MemberCommandPort;
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MemberJpa implements MemberQueryPort, MemberCommandPort, MemberLookupPort, BackofficeOrderMemberLookupPort {

    private final MemberRepository memberRepository;

    @Override
    public Member findByIdOrThrow(Long id) {
        return memberRepository.findById(id).orElseThrow(
                () -> new MemberException(MemberErrorType.MEMBER_NOT_FOUND)
        );
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findByProviderAndProviderId(String provider, String providerId) {
        return memberRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public Map<Long, BackofficeOrderMemberLookupResult> findMembersByIds(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        return memberRepository.findByIdIn(memberIds).stream()
                .map(member -> BackofficeOrderMemberLookupResult.of(
                        member.getId(),
                        member.getName(),
                        member.getEmail()
                ))
                .collect(Collectors.toMap(
                        BackofficeOrderMemberLookupResult::memberId,
                        Function.identity()
                ));
    }

    @Override
    public List<Long> findMemberIdsByKeyword(String keyword) {
        return memberRepository.findIdsByNameOrEmailKeyword(keyword);
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }
}
