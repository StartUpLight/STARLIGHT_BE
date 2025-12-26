package starlight.adapter.expert.webapi.dto;

import starlight.application.expert.provided.dto.ExpertCareerResult;

import java.time.LocalDateTime;

public record ExpertCareerResponse(
        Long id,

        Integer orderIndex,

        String careerTitle,

        String careerExplanation,

        LocalDateTime careerStartedAt,

        LocalDateTime careerEndedAt
) {
    public static ExpertCareerResponse from(ExpertCareerResult result) {
        return new ExpertCareerResponse(
                result.id(),
                result.orderIndex(),
                result.careerTitle(),
                result.careerExplanation(),
                result.careerStartedAt(),
                result.careerEndedAt()
        );
    }
}
