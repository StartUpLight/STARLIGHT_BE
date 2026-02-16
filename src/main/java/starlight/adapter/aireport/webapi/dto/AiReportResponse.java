package starlight.adapter.aireport.webapi.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.List;
import java.util.Objects;
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
        List<AiReportResult.SectionScoreDetailResponse> sourceSectionScores = 
                Objects.requireNonNullElse(result.sectionScores(), List.<AiReportResult.SectionScoreDetailResponse>of());
        List<SectionScoreDetailResponse> sectionScores = sourceSectionScores.stream()
                .map(s -> new SectionScoreDetailResponse(s.sectionType(), s.gradingListScores()))
                .toList();
        
        List<AiReportResult.StrengthWeakness> sourceStrengths = 
                Objects.requireNonNullElse(result.strengths(), List.<AiReportResult.StrengthWeakness>of());
        List<StrengthWeakness> strengths = sourceStrengths.stream()
                .map(s -> new StrengthWeakness(s.title(), s.content()))
                .toList();
        
        List<AiReportResult.StrengthWeakness> sourceWeaknesses = 
                Objects.requireNonNullElse(result.weaknesses(), List.<AiReportResult.StrengthWeakness>of());
        List<StrengthWeakness> weaknesses = sourceWeaknesses.stream()
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
