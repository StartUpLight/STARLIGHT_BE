package starlight.application.backoffice.businessplan.required.dto;

import java.time.LocalDateTime;

public record BackofficeBusinessPlanMemberLookupResult(
        Long memberId,
        String name,
        String email,
        String provider,
        LocalDateTime joinedAt,
        LocalDateTime lastActiveAt
) {
    public static BackofficeBusinessPlanMemberLookupResult of(
            Long memberId,
            String name,
            String email,
            String provider
    ) {
        return new BackofficeBusinessPlanMemberLookupResult(memberId, name, email, provider, null, null);
    }

    public static BackofficeBusinessPlanMemberLookupResult of(
            Long memberId,
            String name,
            String email,
            String provider,
            LocalDateTime joinedAt,
            LocalDateTime lastActiveAt
    ) {
        return new BackofficeBusinessPlanMemberLookupResult(memberId, name, email, provider, joinedAt, lastActiveAt);
    }
}
