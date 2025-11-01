package starlight.adapter.expert.dto;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record ExpertListResponse(

        Long id,

        String name,

        String profileImageUrl,

        Long workedPeriod,

        String email,

        Integer mentoringPriceWon,

        List<String> careers,

        List<String> tags
) {
    public static ExpertListResponse from(Expert expert) {
        return new ExpertListResponse(
                expert.getId(),
                expert.getName(),
                expert.getProfileImageUrl(),
                expert.getWorkedPeriod(),
                expert.getEmail(),
                expert.getMentoringPriceWon(),
                expert.getCareers(),
                expert.getTags().stream().distinct().toList()
        );
    }

    public static List<ExpertListResponse> fromAll(Collection<Expert> experts){
        return experts.stream().map(ExpertListResponse::from).toList();
    }
}
