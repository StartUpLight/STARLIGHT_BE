package starlight.adapter.aireport.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.AiReportCommandPort;
import starlight.application.aireport.required.AiReportQueryPort;
import starlight.application.expert.required.AiReportSummaryLookupPort;
import starlight.domain.aireport.entity.AiReport;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiReportJpa implements AiReportCommandPort, AiReportQueryPort, AiReportSummaryLookupPort {

    private final AiReportRepository aiReportRepository;

    @Override
    public AiReport save(AiReport aiReport) {
        return aiReportRepository.save(aiReport);
    }

    @Override
    public Optional<AiReport> findByBusinessPlanId(Long businessPlanId) {
        return aiReportRepository.findByBusinessPlanId(businessPlanId);
    }

    @Override
    public Map<Long, Integer> findTotalScoresByBusinessPlanIds(List<Long> businessPlanIds) {
        if (businessPlanIds == null || businessPlanIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AiReport> reports = aiReportRepository.findAllByBusinessPlanIdIn(businessPlanIds);
        Map<Long, Integer> totalScoreMap = new HashMap<>();

        for (AiReport report : reports) {
            Integer totalScore = AiReportResult.from(report).totalScore();
            totalScoreMap.put(report.getBusinessPlanId(), totalScore != null ? totalScore : 0);
        }

        return totalScoreMap;
    }
}
