package starlight.application.backoffice.expert.provided;

import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertActiveStatusUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertCreateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertProfileImageUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertUpdateInput;
import starlight.application.backoffice.expert.provided.dto.result.BackofficeExpertCreateResult;

public interface BackofficeExpertCommandUseCase {

    BackofficeExpertCreateResult createExpert(BackofficeExpertCreateInput input);

    void updateExpert(BackofficeExpertUpdateInput input);

    void deleteExpert(Long expertId);

    void updateActiveStatus(BackofficeExpertActiveStatusUpdateInput input);

    void updateProfileImage(BackofficeExpertProfileImageUpdateInput input);
}
