package starlight.adapter.backoffice.expert.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertCareerUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertUpdateInput;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.List;

public record BackofficeExpertUpdateRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String oneLineIntroduction,
        String detailedIntroduction,
        Long workedPeriod,
        Integer mentoringPriceWon,
        List<String> tags,
        List<TagCategory> categories,
        @Valid List<BackofficeExpertCareerUpdateRequest> careers
) {
    public BackofficeExpertUpdateInput toInput(Long expertId) {
        List<BackofficeExpertCareerUpdateInput> careerInputs = careers == null
                ? null
                : careers.stream()
                        .map(career -> new BackofficeExpertCareerUpdateInput(
                                career.id(),
                                career.orderIndex(),
                                career.careerTitle(),
                                career.careerExplanation(),
                                career.careerStartedAt(),
                                career.careerEndedAt()
                        ))
                        .toList();

        return BackofficeExpertUpdateInput.of(
                expertId,
                name,
                email,
                oneLineIntroduction,
                detailedIntroduction,
                workedPeriod,
                mentoringPriceWon,
                tags,
                categories,
                careerInputs
        );
    }
}
