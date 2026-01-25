package starlight.adapter.backoffice.expert.webapi.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BackofficeExpertCareerUpdateRequest(
        Long id,
        @NotNull @Min(0) Integer orderIndex,
        @NotBlank String careerTitle,
        String careerExplanation,
        @NotNull LocalDateTime careerStartedAt,
        @NotNull LocalDateTime careerEndedAt
) {
    @AssertTrue(message = "경력 시작일은 종료일보다 늦을 수 없습니다.")
    public boolean isValidPeriod() {
        if (careerStartedAt == null || careerEndedAt == null) {
            return true;
        }
        return !careerStartedAt.isAfter(careerEndedAt);
    }
}
