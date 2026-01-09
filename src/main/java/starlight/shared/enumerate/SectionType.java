package starlight.shared.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import starlight.application.aireport.provided.dto.AiReportResult;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum SectionType {

    OVERVIEW("개요", null, null, null),
    PROBLEM_RECOGNITION("문제 인식", "problem_recognition", "PROBLEM_RECOGNITION", AiReportResult::problemRecognitionScore),
    FEASIBILITY("실현 가능성", "feasibility", "FEASIBILITY", AiReportResult::feasibilityScore),
    GROWTH_STRATEGY("성장 전략", "growth_strategy", "GROWTH_STRATEGY", AiReportResult::growthStrategyScore),
    TEAM_COMPETENCE("팀 역량", "team_competence", "TEAM_COMPETENCE", AiReportResult::teamCompetenceScore);

    private final String description;
    private final String tag;
    private final String sectionTypeString;  // sectionScores에서 사용할 문자열
    private final Function<AiReportResult, Integer> scoreExtractor;  // score 추출 함수

    /**
     * AiReportResponse에서 해당 섹션의 score를 추출
     */
    public Integer extractScore(AiReportResult response) {
        if (scoreExtractor == null || response == null) {
            return 0;
        }
        return scoreExtractor.apply(response);
    }
}