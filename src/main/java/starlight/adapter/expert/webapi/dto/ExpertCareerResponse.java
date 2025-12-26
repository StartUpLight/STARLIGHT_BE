package starlight.adapter.expert.webapi.dto;

import starlight.domain.expert.entity.ExpertCareer;

import java.time.LocalDateTime;

public record ExpertCareerResponse(
        Long id,

        Integer orderIndex,

        String careerTitle,

        String careerExplanation,

        LocalDateTime careerStartedAt,

        LocalDateTime careerEndedAt
) {
    public static ExpertCareerResponse from(ExpertCareer expertCareer) {
        return new ExpertCareerResponse(
                expertCareer.getId(),
                expertCareer.getOrderIndex(),
                expertCareer.getCareerTitle(),
                expertCareer.getCareerExplanation(),
                expertCareer.getCareerStartedAt(),
                expertCareer.getCareerEndedAt()
        );
    }
}
