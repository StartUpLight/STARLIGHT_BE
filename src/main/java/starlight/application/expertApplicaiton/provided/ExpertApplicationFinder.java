package starlight.application.expertApplicaiton.provided;

import starlight.domain.expertApplication.entity.ExpertApplication;

import java.util.List;

public interface ExpertApplicationFinder {

    List<Long> findRequestedExpertIds(Long businessPlanId);

    ExpertApplication save(ExpertApplication application);
}
