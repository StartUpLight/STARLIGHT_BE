package starlight.adapter.expert.dto;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;

public record ExpertListResponse(

        Long id,

        String name,

        String profileImageUrl,

        String email,

        Integer mentoringPriceWon,

        List<String> careers,

        List<String> categories
) {
    public static ExpertListResponse from(Expert expert) {
        List<String> labels = expert.getCategories().stream()
                .map(TagCategory::getDescription)
                .toList();

        return new ExpertListResponse(
                expert.getId(),
                expert.getName(),
                expert.getProfileImageUrl(),
                expert.getEmail(),
                expert.getMentoringPriceWon(),
                expert.getCareers(),
                labels
        );
    }

    public static List<ExpertListResponse> fromAll(Collection<Expert> experts){
        return experts.stream().map(ExpertListResponse::from).toList();
    }
}
