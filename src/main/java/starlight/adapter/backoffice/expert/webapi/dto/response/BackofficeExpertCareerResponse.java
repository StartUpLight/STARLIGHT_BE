package starlight.adapter.backoffice.expert.webapi.dto.response;

import starlight.application.expert.provided.dto.ExpertCareerResult;

import java.time.LocalDateTime;

public record BackofficeExpertCareerResponse(
        Long id,
        Integer orderIndex,
        String careerTitle,
        String careerExplanation,
        LocalDateTime careerStartedAt,
        LocalDateTime careerEndedAt
) {
    public static BackofficeExpertCareerResponse from(ExpertCareerResult result) {
        return new BackofficeExpertCareerResponse(
                result.id(),
                result.orderIndex(),
                result.careerTitle(),
                result.careerExplanation(),
                result.careerStartedAt(),
                result.careerEndedAt()
        );
    }
}
