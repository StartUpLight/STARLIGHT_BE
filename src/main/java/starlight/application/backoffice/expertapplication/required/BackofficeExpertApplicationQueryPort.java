package starlight.application.backoffice.expertapplication.required;

import starlight.application.backoffice.expertapplication.required.dto.BackofficeExpertApplicationLookupResult;

import java.util.List;

public interface BackofficeExpertApplicationQueryPort {

    List<BackofficeExpertApplicationLookupResult> findByBusinessPlanId(Long businessPlanId);
}
