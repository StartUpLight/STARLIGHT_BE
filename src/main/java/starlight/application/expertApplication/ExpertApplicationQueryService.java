package starlight.application.expertApplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expertApplication.provided.ExpertApplicationQueryUseCase;
import starlight.application.expertApplication.required.ExpertApplicationQuery;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertApplicationQueryService implements ExpertApplicationQueryUseCase {

    private final ExpertApplicationQuery expertApplicationQuery;

    @Override
    public List<Long> findRequestedExpertIds(Long businessPlanId) {
        return expertApplicationQuery.findRequestedExpertIds(businessPlanId);
    }
}
