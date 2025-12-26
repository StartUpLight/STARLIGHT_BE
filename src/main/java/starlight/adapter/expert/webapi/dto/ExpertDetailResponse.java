package starlight.adapter.expert.webapi.dto;

import starlight.application.expert.provided.dto.ExpertCareerResult;
import starlight.application.expert.provided.dto.ExpertDetailResult;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record ExpertDetailResponse(

        Long id,

        Long applicationCount,

        String name,

        String oneLineIntroduction,

        String detailedIntroduction,

        String profileImageUrl,

        Long workedPeriod,

        String email,

        Integer mentoringPriceWon,

        List<ExpertCareerResponse> careers,

        List<String> tags,

        List<String> categories
) {
    public static ExpertDetailResponse from(Expert expert, long applicationCount) {
        List<ExpertCareerResponse> careers = expert.getCareers().stream()
                .map(ExpertCareerResponse::from)
                .toList();

        List <String> categories = expert.getCategories().stream()
                .map(TagCategory::name)
                .distinct()
                .toList();

        List<String> tags = expert.getTags().stream()
                .distinct()
                .toList();

        return new ExpertDetailResponse(
                expert.getId(),
                applicationCount,
                expert.getName(),
                expert.getOneLineIntroduction(),
                expert.getDetailedIntroduction(),
                expert.getProfileImageUrl(),
                expert.getWorkedPeriod(),
                expert.getEmail(),
                expert.getMentoringPriceWon(),
                careers,
                tags,
                categories
        );
    }

    public static ExpertDetailResponse from(Expert expert) {
        return from(expert, 0L);
    }

    public static ExpertDetailResponse from(ExpertDetailResult result) {
        List<ExpertCareerResponse> careers = result.careers().stream()
                .map(ExpertDetailResponse::toCareerResponse)
                .toList();

        return new ExpertDetailResponse(
                result.id(),
                result.applicationCount(),
                result.name(),
                result.oneLineIntroduction(),
                result.detailedIntroduction(),
                result.profileImageUrl(),
                result.workedPeriod(),
                result.email(),
                result.mentoringPriceWon(),
                careers,
                result.tags(),
                result.categories()
        );
    }

    public static List<ExpertDetailResponse> fromAllResults(Collection<ExpertDetailResult> results) {
        return results.stream()
                .map(ExpertDetailResponse::from)
                .toList();
    }

    private static ExpertCareerResponse toCareerResponse(ExpertCareerResult result) {
        return new ExpertCareerResponse(
                result.id(),
                result.orderIndex(),
                result.careerTitle(),
                result.careerExplanation(),
                result.careerStartedAt(),
                result.careerEndedAt()
        );
    }

    public static List<ExpertDetailResponse> fromAll(Collection<Expert> experts, Map<Long, Long> countMap) {
        return experts.stream()
                .map(e -> ExpertDetailResponse.from(e, countMap.getOrDefault(e.getId(), 0L)))
                .toList();
    }
}
