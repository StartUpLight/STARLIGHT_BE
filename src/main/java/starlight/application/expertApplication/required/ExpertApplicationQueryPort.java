package starlight.application.expertApplication.required;

import starlight.domain.expertApplication.entity.ExpertApplication;

public interface ExpertApplicationQueryPort {
    Boolean existsByExpertIdAndBusinessPlanId(Long expertId, Long businessPlanId);

    ExpertApplication save(ExpertApplication application);
}
