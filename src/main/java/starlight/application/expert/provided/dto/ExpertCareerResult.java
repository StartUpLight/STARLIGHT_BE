package starlight.application.expert.provided.dto;

import starlight.domain.expert.entity.ExpertCareer;

import java.time.LocalDateTime;

public record ExpertCareerResult(
        Long id,
        Integer orderIndex,
        String careerTitle,
        String careerExplanation,
        LocalDateTime careerStartedAt,
        LocalDateTime careerEndedAt
) {
    public static ExpertCareerResult from(ExpertCareer expertCareer) {
        return new ExpertCareerResult(
                expertCareer.getId(),
                expertCareer.getOrderIndex(),
                expertCareer.getCareerTitle(),
                expertCareer.getCareerExplanation(),
                expertCareer.getCareerStartedAt(),
                expertCareer.getCareerEndedAt()
        );
    }
}
