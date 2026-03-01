package starlight.adapter.aireport.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.AiReportCommandPort;
import starlight.application.aireport.required.AiReportQueryPort;
import starlight.application.backoffice.businessplan.required.BackofficeBusinessPlanScoreLookupPort;
import starlight.application.backoffice.member.required.BackofficeUserBusinessPlanScoreLookupPort;
import starlight.application.expert.required.AiReportSummaryLookupPort;
import starlight.domain.aireport.entity.AiReport;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiReportJpa implements AiReportCommandPort, AiReportQueryPort,
        AiReportSummaryLookupPort, BackofficeBusinessPlanScoreLookupPort,
        BackofficeUserBusinessPlanScoreLookupPort {

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
            totalScoreMap.put(report.getBusinessPlanId(), AiReportResult.from(report).totalScore());
        }

        return totalScoreMap;
    }

    @Override
    public Map<Long, Integer> findScoresByBusinessPlanIds(Collection<Long> businessPlanIds) {
        if (businessPlanIds == null || businessPlanIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AiReport> reports = aiReportRepository.findAllByBusinessPlanIdIn(businessPlanIds);
        Map<Long, Integer> scoreMap = new HashMap<>();

        for (AiReport report : reports) {
            scoreMap.put(report.getBusinessPlanId(), AiReportResult.from(report).totalScore());
        }

        return scoreMap;
    }
}
