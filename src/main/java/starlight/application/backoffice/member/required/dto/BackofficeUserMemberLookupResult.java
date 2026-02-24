package starlight.application.backoffice.member.required.dto;

import java.time.LocalDateTime;

public record BackofficeUserMemberLookupResult(
        Long userId,
        String name,
        String email,
        String provider,
        LocalDateTime joinedAt,
        LocalDateTime lastActiveAt
) {
    public static BackofficeUserMemberLookupResult of(
            Long userId,
            String name,
            String email,
            String provider,
            LocalDateTime joinedAt,
            LocalDateTime lastActiveAt
    ) {
        return new BackofficeUserMemberLookupResult(userId, name, email, provider, joinedAt, lastActiveAt);
    }
}
