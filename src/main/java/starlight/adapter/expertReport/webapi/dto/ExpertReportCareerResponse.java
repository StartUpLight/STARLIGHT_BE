package starlight.adapter.expertReport.webapi.dto;

import starlight.application.expert.provided.dto.ExpertCareerResult;

import java.time.LocalDateTime;

public record ExpertReportCareerResponse(
        Long id,

        Integer orderIndex,

        String careerTitle,

        String careerExplanation,

        LocalDateTime careerStartedAt,

        LocalDateTime careerEndedAt
) {
    public static ExpertReportCareerResponse from(ExpertCareerResult result) {
        return new ExpertReportCareerResponse(
                result.id(),
                result.orderIndex(),
                result.careerTitle(),
                result.careerExplanation(),
                result.careerStartedAt(),
                result.careerEndedAt()
        );
    }
}
