package starlight.adapter.expertReport.webapi.dto;

import starlight.application.expert.provided.dto.ExpertDetailResult;

import java.util.List;

public record ExpertReportExpertResponse(
        Long id,

        Long applicationCount,

        String name,

        String oneLineIntroduction,

        String detailedIntroduction,

        String profileImageUrl,

        Long workedPeriod,

        String email,

        Integer mentoringPriceWon,

        List<ExpertReportCareerResponse> careers,

        List<String> tags
) {
    public static ExpertReportExpertResponse from(ExpertDetailResult result) {
        List<ExpertReportCareerResponse> careers = result.careers().stream()
                .map(ExpertReportCareerResponse::from)
                .toList();

        return new ExpertReportExpertResponse(
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
                result.tags()
        );
    }
}
