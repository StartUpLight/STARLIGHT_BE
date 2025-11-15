package starlight.adapter.expert.webapi.dto;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;

public record ExpertDetailResponse(

        Long id,

        String name,

        String profileImageUrl,

        Long workedPeriod,

        String email,

        Integer mentoringPriceWon,

        List<String> careers,

        List<String> tags,

        List<String> categories
) {
    public static ExpertDetailResponse from(Expert expert) {
        List <String> categories = expert.getCategories().stream()
                .map(TagCategory::name)
                .distinct()
                .toList();

        return new ExpertDetailResponse(
                expert.getId(),
                expert.getName(),
                expert.getProfileImageUrl(),
                expert.getWorkedPeriod(),
                expert.getEmail(),
                expert.getMentoringPriceWon(),
                expert.getCareers(),
                expert.getTags().stream().distinct().toList(),
                categories
        );
    }

    public static List<ExpertDetailResponse> fromAll(Collection<Expert> experts){
        return experts.stream().map(ExpertDetailResponse::from).toList();
    }
}
