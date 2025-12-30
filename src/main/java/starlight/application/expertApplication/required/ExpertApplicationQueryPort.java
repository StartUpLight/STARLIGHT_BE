package starlight.application.expertApplication.required;

import starlight.domain.expertApplication.entity.ExpertApplication;

import java.util.List;

public interface ExpertApplicationQueryPort {
    Boolean existsByExpertIdAndBusinessPlanId(Long expertId, Long businessPlanId);

    List<Long> findRequestedExpertIds(Long businessPlanId);

    ExpertApplication save(ExpertApplication application);
}
