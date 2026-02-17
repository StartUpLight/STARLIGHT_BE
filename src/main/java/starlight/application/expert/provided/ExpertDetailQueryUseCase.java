package starlight.application.expert.provided;

import starlight.application.expert.provided.dto.ExpertDetailResult;

import java.util.List;

public interface ExpertDetailQueryUseCase {

    List<ExpertDetailResult> searchAllActive();

    ExpertDetailResult findById(Long expertId);
}
