package starlight.application.backoffice.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import starlight.application.backoffice.member.provided.BackofficeUserQueryUseCase;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserBusinessPlanPageResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserBusinessPlanRowResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserDashboardResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPageResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPaymentResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPaymentRowResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserRowResult;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserWalletResult;
import starlight.application.backoffice.member.required.BackofficeUserBusinessPlanLookupPort;
import starlight.application.backoffice.member.required.BackofficeUserBusinessPlanScoreLookupPort;
import starlight.application.backoffice.member.required.BackofficeUserMemberLookupPort;
import starlight.application.backoffice.member.required.BackofficeUserPaymentLookupPort;
import starlight.application.backoffice.member.required.BackofficeUserWalletLookupPort;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanMemberLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserMemberLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserPaymentLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserSignupLookupResult;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeUserQueryService implements BackofficeUserQueryUseCase {

    private static final int DASHBOARD_MONTH_WINDOW = 12;

    private final BackofficeUserMemberLookupPort userMemberLookupPort;
    private final BackofficeUserBusinessPlanLookupPort userBusinessPlanLookupPort;
    private final BackofficeUserBusinessPlanScoreLookupPort userBusinessPlanScoreLookupPort;
    private final BackofficeUserPaymentLookupPort userPaymentLookupPort;
    private final BackofficeUserWalletLookupPort userWalletLookupPort;

    @Override
    public BackofficeUserDashboardResult getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        long totalUsers = userMemberLookupPort.countUsers();
        long activeUsers = userMemberLookupPort.countActiveUsersSince(today.minusDays(30).atStartOfDay());
        long newUsersThisMonth = userMemberLookupPort.countNewUsersBetween(monthStart, nextMonthStart);
        long churnedUsersThisMonth = userMemberLookupPort.countChurnedUsersBetween(monthStart, nextMonthStart);

        LocalDateTime monthlyFrom = monthStart.minusMonths(DASHBOARD_MONTH_WINDOW - 1L);
        List<BackofficeUserSignupLookupResult> signups = userMemberLookupPort.findSignupsBetween(monthlyFrom, nextMonthStart);

        List<BackofficeUserDashboardResult.MonthlySignupResult> monthlySignups = buildMonthlySignups(signups, monthlyFrom);
        List<BackofficeUserDashboardResult.SignupChannelResult> signupChannels = buildSignupChannels(signups, monthStart);

        return BackofficeUserDashboardResult.of(
                totalUsers,
                activeUsers,
                newUsersThisMonth,
                churnedUsersThisMonth,
                monthlySignups,
                signupChannels
        );
    }

    @Override
    public BackofficeUserPageResult findUsers(String keyword, Pageable pageable) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<BackofficeUserMemberLookupResult> userPage = userMemberLookupPort.findUserPage(normalizedKeyword, pageable);

        List<Long> userIds = userPage.getContent().stream()
                .map(BackofficeUserMemberLookupResult::userId)
                .filter(Objects::nonNull)
                .toList();

        List<BackofficeUserBusinessPlanMemberLookupResult> businessPlanRows =
                userBusinessPlanLookupPort.findBusinessPlansByUserIds(userIds);

        Map<Long, Long> businessPlanCountMap = businessPlanRows.stream()
                .collect(Collectors.groupingBy(
                        BackofficeUserBusinessPlanMemberLookupResult::userId,
                        Collectors.counting()
                ));

        List<Long> businessPlanIds = businessPlanRows.stream()
                .map(BackofficeUserBusinessPlanMemberLookupResult::businessPlanId)
                .toList();

        Map<Long, Integer> scoreMap = userBusinessPlanScoreLookupPort.findScoresByBusinessPlanIds(businessPlanIds);
        Map<Long, List<Integer>> scoreByUserMap = new LinkedHashMap<>();

        for (BackofficeUserBusinessPlanMemberLookupResult row : businessPlanRows) {
            Integer score = scoreMap.get(row.businessPlanId());
            if (score == null) {
                continue;
            }

            scoreByUserMap.computeIfAbsent(row.userId(), key -> new ArrayList<>()).add(score);
        }

        List<BackofficeUserRowResult> content = userPage.getContent().stream()
                .map(user -> BackofficeUserRowResult.of(
                        user.userId(),
                        user.name(),
                        user.email(),
                        user.joinedAt(),
                        user.lastActiveAt(),
                        user.provider(),
                        businessPlanCountMap.getOrDefault(user.userId(), 0L),
                        computeAverageScore(scoreByUserMap.get(user.userId()))
                ))
                .toList();

        return BackofficeUserPageResult.of(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext()
        );
    }

    @Override
    public BackofficeUserBusinessPlanPageResult findUserBusinessPlans(Long userId, String scoreFilter, Pageable pageable) {
        ensureUserExists(userId);

        Boolean scored = toScoredFilter(scoreFilter);
        Page<BackofficeUserBusinessPlanLookupResult> planPage =
                userBusinessPlanLookupPort.findUserBusinessPlanPage(userId, scored, pageable);

        List<Long> planIds = planPage.getContent().stream()
                .map(BackofficeUserBusinessPlanLookupResult::planId)
                .toList();

        Map<Long, Integer> scoreMap = userBusinessPlanScoreLookupPort.findScoresByBusinessPlanIds(planIds);

        List<BackofficeUserBusinessPlanRowResult> content = planPage.getContent().stream()
                .map(plan -> BackofficeUserBusinessPlanRowResult.of(
                        plan.planId(),
                        plan.title(),
                        plan.planStatus(),
                        scoreMap.get(plan.planId()),
                        plan.updatedAt()
                ))
                .toList();

        return BackofficeUserBusinessPlanPageResult.of(
                content,
                planPage.getNumber(),
                planPage.getSize(),
                planPage.getTotalElements(),
                planPage.getTotalPages(),
                planPage.hasNext()
        );
    }

    @Override
    public BackofficeUserPaymentResult findUserPayments(Long userId, Pageable pageable) {
        ensureUserExists(userId);

        BackofficeUserWalletResult wallet = userWalletLookupPort.findWalletByUserId(userId)
                .map(value -> BackofficeUserWalletResult.of(
                        value.chargedCount(),
                        value.usedCount(),
                        value.remainingCount()
                ))
                .orElseGet(() -> BackofficeUserWalletResult.of(0L, 0L, 0L));

        Page<BackofficeUserPaymentLookupResult> paymentPage = userPaymentLookupPort.findUserPaymentPage(userId, pageable);

        List<BackofficeUserPaymentRowResult> payments = paymentPage.getContent().stream()
                .map(payment -> BackofficeUserPaymentRowResult.of(
                        payment.orderId(),
                        payment.orderCode(),
                        payment.usageProductCode(),
                        payment.usageCount(),
                        payment.price(),
                        payment.orderStatus(),
                        payment.latestPaymentStatus(),
                        payment.paymentKey(),
                        payment.createdAt(),
                        toLocalDateTime(payment.approvedAt())
                ))
                .toList();

        return BackofficeUserPaymentResult.of(
                wallet,
                payments,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.hasNext()
        );
    }

    private void ensureUserExists(Long userId) {
        if (userId == null || !userMemberLookupPort.existsById(userId)) {
            throw new MemberException(MemberErrorType.MEMBER_NOT_FOUND);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return keyword.trim();
    }

    private Boolean toScoredFilter(String scoreFilter) {
        if (!StringUtils.hasText(scoreFilter)) {
            return null;
        }

        String normalized = scoreFilter.trim().toLowerCase();
        return switch (normalized) {
            case "scored" -> true;
            case "unscored" -> false;
            default -> null;
        };
    }

    private Double computeAverageScore(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return null;
        }

        double average = scores.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return roundToSingleDecimal(average);
    }

    private List<BackofficeUserDashboardResult.MonthlySignupResult> buildMonthlySignups(
            List<BackofficeUserSignupLookupResult> signups,
            LocalDateTime monthlyFrom
    ) {
        YearMonth start = YearMonth.from(monthlyFrom);

        Map<YearMonth, Long> countByMonth = signups.stream()
                .filter(signup -> signup.joinedAt() != null)
                .collect(Collectors.groupingBy(
                        signup -> YearMonth.from(signup.joinedAt()),
                        Collectors.counting()
                ));

        List<BackofficeUserDashboardResult.MonthlySignupResult> monthlySignups = new ArrayList<>();
        for (int i = 0; i < DASHBOARD_MONTH_WINDOW; i++) {
            YearMonth target = start.plusMonths(i);
            monthlySignups.add(BackofficeUserDashboardResult.MonthlySignupResult.of(
                    target.toString(),
                    countByMonth.getOrDefault(target, 0L)
            ));
        }

        return monthlySignups;
    }

    private List<BackofficeUserDashboardResult.SignupChannelResult> buildSignupChannels(
            List<BackofficeUserSignupLookupResult> signups,
            LocalDateTime monthStart
    ) {
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        Map<String, Long> currentMap = signups.stream()
                .filter(signup -> isBetween(signup.joinedAt(), monthStart, nextMonthStart))
                .collect(Collectors.groupingBy(
                        signup -> normalizeProvider(signup.provider()),
                        Collectors.counting()
                ));

        Map<String, Long> previousMap = signups.stream()
                .filter(signup -> isBetween(signup.joinedAt(), prevMonthStart, monthStart))
                .collect(Collectors.groupingBy(
                        signup -> normalizeProvider(signup.provider()),
                        Collectors.counting()
                ));

        Set<String> providers = Set.copyOf(List.of("starlight", "kakao"));
        Set<String> dynamicProviders = Set.copyOf(currentMap.keySet());
        Set<String> previousProviders = Set.copyOf(previousMap.keySet());

        List<String> orderedProviders = new ArrayList<>();
        orderedProviders.addAll(providers);

        List<String> additionalProviders = new ArrayList<>();
        additionalProviders.addAll(dynamicProviders);
        additionalProviders.addAll(previousProviders);
        additionalProviders.removeAll(providers);
        additionalProviders = additionalProviders.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        orderedProviders.addAll(additionalProviders);

        List<BackofficeUserDashboardResult.SignupChannelResult> channels = orderedProviders.stream()
                .map(provider -> {
                    long current = currentMap.getOrDefault(provider, 0L);
                    long previous = previousMap.getOrDefault(provider, 0L);
                    double growthRate = previous == 0L
                            ? 0.0
                            : ((double) (current - previous) / (double) previous) * 100.0;

                    return BackofficeUserDashboardResult.SignupChannelResult.of(
                            provider,
                            current,
                            roundToSingleDecimal(growthRate)
                    );
                })
                .filter(channel -> channel.value() > 0 || providers.contains(channel.provider()))
                .toList();

        return channels;
    }

    private boolean isBetween(LocalDateTime target, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        if (target == null) {
            return false;
        }

        return !target.isBefore(fromInclusive) && target.isBefore(toExclusive);
    }

    private LocalDateTime toLocalDateTime(java.time.Instant instant) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
    }

    private String normalizeProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            return "unknown";
        }

        return provider.trim().toLowerCase();
    }

    private double roundToSingleDecimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
