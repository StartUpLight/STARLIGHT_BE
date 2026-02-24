package starlight.adapter.backoffice.expertapplication.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.expertapplication.required.BackofficeExpertApplicationQueryPort;
import starlight.application.backoffice.expertapplication.required.dto.BackofficeExpertApplicationLookupResult;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BackofficeBusinessPlanExpertApplicationQueryJpa implements BackofficeExpertApplicationQueryPort {

    private final BackofficeBusinessPlanExpertApplicationRepository repository;

    @Override
    public List<BackofficeExpertApplicationLookupResult> findByBusinessPlanId(Long businessPlanId) {
        return repository.findByBusinessPlanId(businessPlanId);
    }
}
