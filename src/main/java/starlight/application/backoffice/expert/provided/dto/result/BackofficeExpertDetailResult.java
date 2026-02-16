package starlight.application.backoffice.expert.provided.dto.result;

import starlight.application.expert.provided.dto.ExpertCareerResult;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.ExpertActiveStatus;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.List;

public record BackofficeExpertDetailResult(
        Long id,
        Long applicationCount,
        String name,
        String oneLineIntroduction,
        String detailedIntroduction,
        String profileImageUrl,
        Long workedPeriod,
        String email,
        Integer mentoringPriceWon,
        ExpertActiveStatus activeStatus,
        List<ExpertCareerResult> careers,
        List<String> tags,
        List<String> categories
) {
    public static BackofficeExpertDetailResult from(Expert expert, long applicationCount) {
        List<ExpertCareerResult> careers = expert.getCareers().stream()
                .map(ExpertCareerResult::from)
                .toList();

        List<String> categories = expert.getCategories().stream()
                .map(TagCategory::name)
                .distinct()
                .toList();

        List<String> tags = expert.getTags().stream()
                .distinct()
                .toList();

        return new BackofficeExpertDetailResult(
                expert.getId(),
                applicationCount,
                expert.getName(),
                expert.getOneLineIntroduction(),
                expert.getDetailedIntroduction(),
                expert.getProfileImageUrl(),
                expert.getWorkedPeriod(),
                expert.getEmail(),
                expert.getMentoringPriceWon(),
                expert.getActiveStatus(),
                careers,
                tags,
                categories
        );
    }
}
