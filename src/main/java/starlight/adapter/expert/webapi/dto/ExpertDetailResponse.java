package starlight.adapter.expert.webapi.dto;

import starlight.application.expert.provided.dto.ExpertDetailResult;
import java.util.List;

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
    public static ExpertDetailResponse from(ExpertDetailResult result) {
        List<ExpertCareerResponse> careers = result.careers().stream()
                .map(ExpertCareerResponse::from)
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

    public static List<ExpertDetailResponse> fromAllResults(List<ExpertDetailResult> results) {
        return results.stream()
                .map(ExpertDetailResponse::from)
                .toList();
    }
}
