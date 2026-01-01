package starlight.application.expert.required;

import java.util.List;
import java.util.Map;

public interface AiReportSummaryLookupPort {

    Map<Long, Integer> findTotalScoresByBusinessPlanIds(List<Long> businessPlanIds);
}
