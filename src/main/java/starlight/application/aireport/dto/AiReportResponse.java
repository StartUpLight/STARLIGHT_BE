package starlight.application.aireport.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.List;

/**
 * AI 리포트 응답 DTO
 * LLM 채점 결과와 API 응답을 모두 담는 통합 DTO
 */
public record AiReportResponse(
        Long id,  // null 가능 (LLM 결과 파싱 시에는 null)
        Long businessPlanId,  // null 가능 (LLM 결과 파싱 시에는 null)
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
    
    /**
     * LLM 결과만으로 AiReportResponse 생성 (id, businessPlanId는 null)
     */
    public static AiReportResponse fromGradingResult(
            Integer problemRecognitionScore,
            Integer feasibilityScore,
            Integer growthStrategyScore,
            Integer teamCompetenceScore,
            List<SectionScoreDetailResponse> sectionScores,
            List<StrengthWeakness> strengths,
            List<StrengthWeakness> weaknesses
    ) {
        Integer totalScore = (problemRecognitionScore != null ? problemRecognitionScore : 0) +
                           (feasibilityScore != null ? feasibilityScore : 0) +
                           (growthStrategyScore != null ? growthStrategyScore : 0) +
                           (teamCompetenceScore != null ? teamCompetenceScore : 0);
        
        return new AiReportResponse(
                null,
                null,
                totalScore,
                problemRecognitionScore,
                feasibilityScore,
                growthStrategyScore,
                teamCompetenceScore,
                sectionScores,
                strengths,
                weaknesses
        );
    }
}

