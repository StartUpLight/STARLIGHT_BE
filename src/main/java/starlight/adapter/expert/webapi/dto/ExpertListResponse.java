package starlight.adapter.expert.webapi.dto;

import starlight.application.expert.provided.dto.ExpertCareerResult;
import starlight.application.expert.provided.dto.ExpertDetailResult;

import java.util.List;

public record ExpertListResponse(
        Long id,
        String name,
        String oneLineIntroduction,
        String profileImageUrl,
        Long workedPeriod,
        String email,
        List<ExpertCareerSummaryResponse> careers,
        List<String> tags,
        List<String> categories
) {
    private static final int MAX_CAREERS = 3;

    public static ExpertListResponse from(ExpertDetailResult result) {
        List<ExpertCareerSummaryResponse> careers = result.careers().stream()
                .limit(MAX_CAREERS)
                .map(ExpertCareerSummaryResponse::from)
                .toList();

        return new ExpertListResponse(
                result.id(),
                result.name(),
                result.oneLineIntroduction(),
                result.profileImageUrl(),
                result.workedPeriod(),
                result.email(),
                careers,
                result.tags(),
                result.categories()
        );
    }

    public static List<ExpertListResponse> fromAll(List<ExpertDetailResult> results) {
        return results.stream()
                .map(ExpertListResponse::from)
                .toList();
    }

    public record ExpertCareerSummaryResponse(
            Integer orderIndex,
            String careerTitle
    ) {
        public static ExpertCareerSummaryResponse from(ExpertCareerResult result) {
            return new ExpertCareerSummaryResponse(
                    result.orderIndex(),
                    result.careerTitle()
            );
        }
    }
}
