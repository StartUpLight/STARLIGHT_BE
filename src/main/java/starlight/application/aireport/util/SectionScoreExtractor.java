package starlight.application.aireport.util;

import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.shared.enumerate.SectionType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SectionScoreExtractor {

    private static final Map<SectionType, Function<AiReportResult, Integer>> SCORE_EXTRACTORS = new HashMap<>();

    static {
        SCORE_EXTRACTORS.put(SectionType.PROBLEM_RECOGNITION, AiReportResult::problemRecognitionScore);
        SCORE_EXTRACTORS.put(SectionType.FEASIBILITY, AiReportResult::feasibilityScore);
        SCORE_EXTRACTORS.put(SectionType.GROWTH_STRATEGY, AiReportResult::growthStrategyScore);
        SCORE_EXTRACTORS.put(SectionType.TEAM_COMPETENCE, AiReportResult::teamCompetenceScore);
    }

    public static Integer extractScore(SectionType sectionType, AiReportResult result) {
        if (sectionType == null || result == null) {
            return 0;
        }

        Function<AiReportResult, Integer> extractor = SCORE_EXTRACTORS.get(sectionType);
        if (extractor == null) {
            return 0;
        }

        Integer score = extractor.apply(result);
        return score != null ? score : 0;
    }
}

