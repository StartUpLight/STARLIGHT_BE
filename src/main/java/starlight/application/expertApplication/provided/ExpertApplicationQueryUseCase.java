package starlight.application.expertApplication.provided;

import java.util.List;

public interface ExpertApplicationQueryUseCase {

    List<Long> findRequestedExpertIds(Long businessPlanId);
}
