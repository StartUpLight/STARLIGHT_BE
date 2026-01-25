package starlight.application.backoffice.expert.provided.dto.input;

import java.time.LocalDateTime;

public record BackofficeExpertCareerUpdateInput(
        Long id,
        Integer orderIndex,
        String careerTitle,
        String careerExplanation,
        LocalDateTime careerStartedAt,
        LocalDateTime careerEndedAt
) {
}
