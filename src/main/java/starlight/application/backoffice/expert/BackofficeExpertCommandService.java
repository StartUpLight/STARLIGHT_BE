package starlight.application.backoffice.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.backoffice.expert.provided.BackofficeExpertCommandUseCase;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertActiveStatusUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertCreateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertProfileImageUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertCareerUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertUpdateInput;
import starlight.application.backoffice.expert.provided.dto.result.BackofficeExpertCreateResult;
import starlight.application.backoffice.expert.required.BackofficeExpertCommandPort;
import starlight.application.backoffice.expert.required.BackofficeExpertQueryPort;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.dto.ExpertCareerUpdate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BackofficeExpertCommandService implements BackofficeExpertCommandUseCase {

    private final BackofficeExpertCommandPort expertCommandPort;
    private final BackofficeExpertQueryPort expertQueryPort;

    @Override
    public BackofficeExpertCreateResult createExpert(BackofficeExpertCreateInput input) {
        Expert expert = Expert.createBackoffice(
                input.name(),
                input.email(),
                input.oneLineIntroduction(),
                input.tags(),
                input.categories()
        );

        Expert savedExpert = expertCommandPort.save(expert);

        return BackofficeExpertCreateResult.from(savedExpert.getId());
    }

    @Override
    public void updateExpert(BackofficeExpertUpdateInput input) {
        Expert expert = expertQueryPort.findByIdWithCareersTagsCategories(input.expertId());

        expert.updateBasicInfo(
                input.name(),
                input.email(),
                input.oneLineIntroduction(),
                input.detailedIntroduction(),
                input.workedPeriod(),
                input.mentoringPriceWon()
        );

        expert.replaceTags(input.tags());
        expert.replaceCategories(input.categories());

        if (input.careers() != null) {
            expert.syncCareers(toCareerUpdates(input.careers()));
        }
    }

    @Override
    public void deleteExpert(Long expertId) {
        Expert expert = expertQueryPort.findByIdOrThrow(expertId);

        expertCommandPort.delete(expert);
    }

    @Override
    public void updateActiveStatus(BackofficeExpertActiveStatusUpdateInput input) {
        Expert expert = expertQueryPort.findByIdOrThrow(input.expertId());

        expert.updateActiveStatus(input.activeStatus());
    }

    @Override
    public void updateProfileImage(BackofficeExpertProfileImageUpdateInput input) {
        Expert expert = expertQueryPort.findByIdOrThrow(input.expertId());

        expert.updateProfileImageUrl(input.profileImageUrl());
    }

    private List<ExpertCareerUpdate> toCareerUpdates(List<BackofficeExpertCareerUpdateInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return List.of();
        }

        return inputs.stream()
                .map(input -> new ExpertCareerUpdate(
                        input.id(),
                        input.orderIndex(),
                        input.careerTitle(),
                        input.careerExplanation(),
                        input.careerStartedAt(),
                        input.careerEndedAt()
                ))
                .toList();
    }
}
