package starlight.adapter.backoffice.expert.webapi.dto.response;

import starlight.application.backoffice.expert.provided.dto.result.BackofficeExpertDetailResult;
import starlight.application.expert.provided.dto.ExpertCareerResult;

import java.util.List;

public record BackofficeExpertListResponse(
        Long id,
        String name,
        String oneLineIntroduction,
        String profileImageUrl,
        Long workedPeriod,
        String email,
        String activeStatus,
        List<BackofficeExpertCareerSummaryResponse> careers,
        List<String> tags,
        List<String> categories
) {
    private static final int MAX_CAREERS = 3;

    public static BackofficeExpertListResponse from(BackofficeExpertDetailResult result) {
        List<BackofficeExpertCareerSummaryResponse> careers = result.careers().stream()
                .limit(MAX_CAREERS)
                .map(BackofficeExpertCareerSummaryResponse::from)
                .toList();

        return new BackofficeExpertListResponse(
                result.id(),
                result.name(),
                result.oneLineIntroduction(),
                result.profileImageUrl(),
                result.workedPeriod(),
                result.email(),
                result.activeStatus().name(),
                careers,
                result.tags(),
                result.categories()
        );
    }

    public static List<BackofficeExpertListResponse> fromAll(List<BackofficeExpertDetailResult> results) {
        return results.stream()
                .map(BackofficeExpertListResponse::from)
                .toList();
    }

    public record BackofficeExpertCareerSummaryResponse(
            Integer orderIndex,
            String careerTitle
    ) {
        public static BackofficeExpertCareerSummaryResponse from(ExpertCareerResult result) {
            return new BackofficeExpertCareerSummaryResponse(
                    result.orderIndex(),
                    result.careerTitle()
            );
        }
    }
}
