package starlight.adapter.backoffice.expert.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertCareerUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertUpdateInput;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record BackofficeExpertUpdateRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String oneLineIntroduction,
        String detailedIntroduction,
        Long workedPeriod,
        Integer mentoringPriceWon,
        List<String> tags,
        List<TagCategory> categories,
        List<@NotNull @Valid BackofficeExpertCareerUpdateRequest> careers
) {
    public BackofficeExpertUpdateInput toInput(Long expertId) {
        List<BackofficeExpertCareerUpdateInput> careerInputs = careers == null
                ? null
                : careers.stream()
                        .filter(Objects::nonNull)
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

    @AssertTrue(message = "종료일이 없는 경력은 최신 경력 1건만 허용됩니다.")
    public boolean isValidCurrentCareer() {
        if (careers == null || careers.isEmpty()) {
            return true;
        }

        List<BackofficeExpertCareerUpdateRequest> careersWithoutEndDate = careers.stream()
                .filter(Objects::nonNull)
                .filter(career -> career.careerEndedAt() == null)
                .toList();

        if (careersWithoutEndDate.isEmpty()) {
            return true;
        }

        if (careersWithoutEndDate.size() > 1) {
            return false;
        }

        Integer maxOrderIndex = careers.stream()
                .filter(Objects::nonNull)
                .map(BackofficeExpertCareerUpdateRequest::orderIndex)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (maxOrderIndex == null) {
            return true;
        }

        return Objects.equals(careersWithoutEndDate.get(0).orderIndex(), maxOrderIndex);
    }
}
