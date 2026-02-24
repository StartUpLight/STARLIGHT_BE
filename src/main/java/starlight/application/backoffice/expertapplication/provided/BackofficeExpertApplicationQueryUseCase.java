package starlight.application.backoffice.expertapplication.provided;

import starlight.application.backoffice.expertapplication.provided.dto.result.BackofficeBusinessPlanExpertApplicationsResult;

public interface BackofficeExpertApplicationQueryUseCase {

    BackofficeBusinessPlanExpertApplicationsResult findByBusinessPlanId(Long businessPlanId);
}
