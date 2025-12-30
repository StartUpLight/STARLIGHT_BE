package starlight.application.expertApplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.expertApplication.provided.ExpertApplicationQueryUseCase;
import starlight.application.expertApplication.required.ExpertApplicationQueryPort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertApplicationQueryService implements ExpertApplicationQueryUseCase {

    private final ExpertApplicationQueryPort expertApplicationQueryPort;
    private final BusinessPlanQuery businessPlanQuery;

    @Override
    public List<Long> findRequestedExpertIds(Long businessPlanId) {
        businessPlanQuery.findByIdOrThrow(businessPlanId);
        return expertApplicationQueryPort.findRequestedExpertIds(businessPlanId);
    }
}
