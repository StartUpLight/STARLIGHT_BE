package starlight.application.expert.provided;

import starlight.application.expert.provided.dto.ExpertAiReportBusinessPlanResult;

import java.util.List;

public interface ExpertAiReportQueryUseCase {

    List<ExpertAiReportBusinessPlanResult> findAiReportBusinessPlans(Long expertId, Long memberId);
}
