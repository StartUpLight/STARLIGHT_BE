package starlight.adapter.aireport.report.dto;

import starlight.application.aireport.provided.dto.AiReportResult.SectionScoreDetailResponse;
import starlight.shared.enumerate.SectionType;

public record SectionGradingResult(
    SectionType sectionType,
    Integer score,
    SectionScoreDetailResponse sectionScore,
    boolean success,
    String errorMessage
) {
    public static SectionGradingResult success(
        SectionType sectionType,
        Integer score,
        SectionScoreDetailResponse sectionScore
    ) {
        return new SectionGradingResult(sectionType, score, sectionScore, true, null);
    }
    
    public static SectionGradingResult failure(SectionType sectionType, String errorMessage) {
        return new SectionGradingResult(sectionType, 0, null, false, errorMessage);
    }
}



