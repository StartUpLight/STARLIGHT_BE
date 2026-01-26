package starlight.application.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expert.provided.ExpertAiReportQueryUseCase;
import starlight.application.expert.provided.dto.ExpertAiReportBusinessPlanResult;
import starlight.application.expert.required.AiReportSummaryLookupPort;
import starlight.application.expert.required.BusinessPlanQueryLookupPort;
import starlight.application.expert.required.ExpertApplicationCountLookupPort;
import starlight.domain.businessplan.entity.BusinessPlan;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertAiReportQueryService implements ExpertAiReportQueryUseCase {

    private final BusinessPlanQueryLookupPort businessPlanQueryLookupPort;
    private final AiReportSummaryLookupPort aiReportSummaryLookupPort;
    private final ExpertApplicationCountLookupPort expertApplicationCountLookupPort;

    @Override
    public List<ExpertAiReportBusinessPlanResult> findAiReportBusinessPlans(Long expertId, Long memberId) {

        List<BusinessPlan> plans = businessPlanQueryLookupPort.findAllByMemberId(memberId);
        if (plans.isEmpty()) {
            return List.of();
        }

        List<Long> planIds = plans.stream()
                .map(BusinessPlan::getId)
                .toList();

        Map<Long, Integer> totalScoreMap = aiReportSummaryLookupPort.findTotalScoresByBusinessPlanIds(planIds);
        if (totalScoreMap.isEmpty()) {
            return List.of();
        }

        List<Long> aiReportPlanIds = totalScoreMap.keySet().stream().toList();
        Map<Long, Long> requestCountMap = expertApplicationCountLookupPort.countByExpertIdAndBusinessPlanIds(expertId, aiReportPlanIds);

        return plans.stream()
                .filter(plan -> totalScoreMap.containsKey(plan.getId()))
                .map(plan -> {
                    Integer totalScore = totalScoreMap.getOrDefault(plan.getId(), 0);
                    boolean isOver70 = totalScore >= 70;
                    Long requestCount = requestCountMap.getOrDefault(plan.getId(), 0L);
                    return new ExpertAiReportBusinessPlanResult(
                            plan.getId(),
                            plan.getTitle(),
                            requestCount,
                            isOver70
                    );
                })
                .collect(Collectors.toList());
    }
}
