package starlight.application.backoffice.member.required.dto;

import java.time.LocalDateTime;

public record BackofficeUserSignupLookupResult(
        LocalDateTime joinedAt,
        String provider
) {
    public static BackofficeUserSignupLookupResult of(LocalDateTime joinedAt, String provider) {
        return new BackofficeUserSignupLookupResult(joinedAt, provider);
    }
}
