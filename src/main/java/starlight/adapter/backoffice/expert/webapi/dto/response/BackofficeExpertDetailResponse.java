package starlight.adapter.backoffice.expert.webapi.dto.response;

import starlight.application.backoffice.expert.provided.dto.result.BackofficeExpertDetailResult;

import java.util.List;

public record BackofficeExpertDetailResponse(
        Long id,
        Long applicationCount,
        String name,
        String oneLineIntroduction,
        String detailedIntroduction,
        String profileImageUrl,
        Long workedPeriod,
        String email,
        Integer mentoringPriceWon,
        String activeStatus,
        List<BackofficeExpertCareerResponse> careers,
        List<String> tags,
        List<String> categories
) {
    public static BackofficeExpertDetailResponse from(BackofficeExpertDetailResult result) {
        List<BackofficeExpertCareerResponse> careers = result.careers().stream()
                .map(BackofficeExpertCareerResponse::from)
                .toList();

        return new BackofficeExpertDetailResponse(
                result.id(),
                result.applicationCount(),
                result.name(),
                result.oneLineIntroduction(),
                result.detailedIntroduction(),
                result.profileImageUrl(),
                result.workedPeriod(),
                result.email(),
                result.mentoringPriceWon(),
                result.activeStatus().name(),
                careers,
                result.tags(),
                result.categories()
        );
    }
}
