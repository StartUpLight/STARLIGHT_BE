package starlight.application.backoffice.expert.provided;

import starlight.application.backoffice.expert.provided.dto.result.BackofficeExpertDetailResult;

import java.util.List;

public interface BackofficeExpertQueryUseCase {

    List<BackofficeExpertDetailResult> searchAll();

    BackofficeExpertDetailResult findById(Long expertId);
}
