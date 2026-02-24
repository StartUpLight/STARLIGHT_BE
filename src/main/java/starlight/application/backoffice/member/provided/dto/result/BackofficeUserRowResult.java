package starlight.application.backoffice.member.provided.dto.result;

import java.time.LocalDateTime;

public record BackofficeUserRowResult(
        Long id,
        String name,
        String email,
        LocalDateTime joinedAt,
        LocalDateTime lastActiveAt,
        String provider,
        Long businessPlanCount,
        Double averageScore
) {
    public static BackofficeUserRowResult of(
            Long id,
            String name,
            String email,
            LocalDateTime joinedAt,
            LocalDateTime lastActiveAt,
            String provider,
            Long businessPlanCount,
            Double averageScore
    ) {
        return new BackofficeUserRowResult(
                id,
                name,
                email,
                joinedAt,
                lastActiveAt,
                provider,
                businessPlanCount,
                averageScore
        );
    }
}
