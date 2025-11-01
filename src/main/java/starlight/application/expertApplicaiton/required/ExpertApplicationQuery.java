package starlight.application.expertApplicaiton.required;

import starlight.domain.expertApplication.entity.ExpertApplication;

import java.util.List;

public interface ExpertApplicationQuery {

    List<Long> findRequestedExpertIds(Long businessPlanId);

    ExpertApplication save(ExpertApplication application);
}
