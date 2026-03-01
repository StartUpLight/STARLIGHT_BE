package starlight.adapter.backoffice.member.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.backoffice.member.provided.dto.result.BackofficeUserRowResult;

import java.time.LocalDateTime;

public record BackofficeUserRowResponse(
        Long id,
        String name,
        String email,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime joinedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime lastActiveAt,
        String provider,
        Long businessPlanCount,
        Double averageScore
) {
    public static BackofficeUserRowResponse from(BackofficeUserRowResult result) {
        return new BackofficeUserRowResponse(
                result.id(),
                result.name(),
                result.email(),
                result.joinedAt(),
                result.lastActiveAt(),
                result.provider(),
                result.businessPlanCount(),
                result.averageScore()
        );
    }
}
