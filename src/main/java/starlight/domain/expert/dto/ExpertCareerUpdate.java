package starlight.domain.expert.dto;

import java.time.LocalDateTime;

public record ExpertCareerUpdate(
        Long id,
        Integer orderIndex,
        String careerTitle,
        String careerExplanation,
        LocalDateTime careerStartedAt,
        LocalDateTime careerEndedAt
) { }
