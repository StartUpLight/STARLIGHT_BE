package starlight.application.backoffice.expert.provided.dto.input;

import starlight.domain.expert.enumerate.TagCategory;

import java.util.List;

public record BackofficeExpertUpdateInput(
        Long expertId,
        String name,
        String email,
        String oneLineIntroduction,
        String detailedIntroduction,
        Long workedPeriod,
        Integer mentoringPriceWon,
        List<String> tags,
        List<TagCategory> categories,
        List<BackofficeExpertCareerUpdateInput> careers
) {
    public static BackofficeExpertUpdateInput of(
            Long expertId,
            String name,
            String email,
            String oneLineIntroduction,
            String detailedIntroduction,
            Long workedPeriod,
            Integer mentoringPriceWon,
            List<String> tags,
            List<TagCategory> categories,
            List<BackofficeExpertCareerUpdateInput> careers
    ) {
        return new BackofficeExpertUpdateInput(
                expertId,
                name,
                email,
                oneLineIntroduction,
                detailedIntroduction,
                workedPeriod,
                mentoringPriceWon,
                tags,
                categories,
                careers
        );
    }
}
