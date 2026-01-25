package starlight.adapter.aireport.webapi.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.List;
import starlight.application.aireport.provided.dto.AiReportResult;

/**
 * AI 리포트 API 응답 DTO (Web 출력용)
 * Application 레이어의 AiReportResult를 변환하여 사용한다.
 */
public record AiReportResponse(
        Long id,
        Long businessPlanId,
        Integer totalScore,
        Integer problemRecognitionScore,
        Integer feasibilityScore,
        Integer growthStrategyScore,
        Integer teamCompetenceScore,
        List<SectionScoreDetailResponse> sectionScores,
        List<StrengthWeakness> strengths,
        List<StrengthWeakness> weaknesses
) {
    public record SectionScoreDetailResponse(
            String sectionType,
            @JsonRawValue String gradingListScores
    ) {}

    public record StrengthWeakness(
            String title,
            String content
    ) {}

    public static AiReportResponse from(AiReportResult result) {
        List<SectionScoreDetailResponse> sectionScores = result.sectionScores().stream()
                .map(s -> new SectionScoreDetailResponse(s.sectionType(), s.gradingListScores()))
                .toList();
        List<StrengthWeakness> strengths = result.strengths().stream()
                .map(s -> new StrengthWeakness(s.title(), s.content()))
                .toList();
        List<StrengthWeakness> weaknesses = result.weaknesses().stream()
                .map(w -> new StrengthWeakness(w.title(), w.content()))
                .toList();

        return new AiReportResponse(
                result.id(),
                result.businessPlanId(),
                result.totalScore(),
                result.problemRecognitionScore(),
                result.feasibilityScore(),
                result.growthStrategyScore(),
                result.teamCompetenceScore(),
                sectionScores,
                strengths,
                weaknesses
        );
    }
}
