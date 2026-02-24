package starlight.adapter.member.persistence;

import lombok.RequiredArgsConstructor;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.businessplan.required.BackofficeBusinessPlanMemberLookupPort;
import starlight.application.backoffice.businessplan.required.dto.BackofficeBusinessPlanMemberLookupResult;
import starlight.application.backoffice.member.required.BackofficeUserMemberLookupPort;
import starlight.application.backoffice.member.required.dto.BackofficeUserMemberLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserSignupLookupResult;
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
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class MemberJpa implements MemberQueryPort, MemberCommandPort, MemberLookupPort,
        BackofficeOrderMemberLookupPort, BackofficeBusinessPlanMemberLookupPort,
        BackofficeUserMemberLookupPort {

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
    public BackofficeBusinessPlanMemberLookupResult findBusinessPlanMemberById(Long memberId) {
        if (memberId == null) {
            return null;
        }

        return memberRepository.findById(memberId)
                .map(member -> BackofficeBusinessPlanMemberLookupResult.of(
                        member.getId(),
                        member.getName(),
                        member.getEmail(),
                        member.getProvider(),
                        member.getCreatedAt(),
                        member.getModifiedAt()
                ))
                .orElse(null);
    }

    @Override
    public Map<Long, BackofficeBusinessPlanMemberLookupResult> findBusinessPlanMembersByIds(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        return memberRepository.findByIdIn(memberIds).stream()
                .map(member -> BackofficeBusinessPlanMemberLookupResult.of(
                        member.getId(),
                        member.getName(),
                        member.getEmail(),
                        member.getProvider(),
                        member.getCreatedAt(),
                        member.getModifiedAt()
                ))
                .collect(Collectors.toMap(
                        BackofficeBusinessPlanMemberLookupResult::memberId,
                        Function.identity()
                ));
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public boolean existsById(Long userId) {
        if (userId == null) {
            return false;
        }

        return memberRepository.existsById(userId);
    }

    @Override
    public Page<BackofficeUserMemberLookupResult> findUserPage(String keyword, Pageable pageable) {
        return memberRepository.findBackofficeUserPage(keyword, pageable)
                .map(member -> BackofficeUserMemberLookupResult.of(
                        member.getId(),
                        member.getName(),
                        member.getEmail(),
                        member.getProvider(),
                        member.getCreatedAt(),
                        member.getModifiedAt()
                ));
    }

    @Override
    public long countUsers() {
        return memberRepository.countUsers();
    }

    @Override
    public long countActiveUsersSince(LocalDateTime since) {
        return memberRepository.countActiveUsersSince(since);
    }

    @Override
    public long countNewUsersBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return memberRepository.countNewUsersBetween(fromInclusive, toExclusive);
    }

    @Override
    public long countChurnedUsersBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return memberRepository.countChurnedUsersBetween(fromInclusive, toExclusive);
    }

    @Override
    public List<BackofficeUserSignupLookupResult> findSignupsBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return memberRepository.findSignupsBetween(fromInclusive, toExclusive).stream()
                .map(member -> BackofficeUserSignupLookupResult.of(member.getCreatedAt(), member.getProvider()))
                .toList();
    }
}
