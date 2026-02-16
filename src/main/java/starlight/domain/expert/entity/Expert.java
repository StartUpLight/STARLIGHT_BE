package starlight.domain.expert.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.expert.enumerate.ExpertActiveStatus;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.domain.expert.dto.ExpertCareerUpdate;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;
import starlight.shared.AbstractEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expert extends AbstractEntity {

    @Column(length = 320)
    private String name;

    @Column
    private Long workedPeriod;

    @Column
    private String profileImageUrl;

    @Column(nullable = false, length = 320)
    private String email;

    @Column
    private String oneLineIntroduction;

    @Column
    private String detailedIntroduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExpertActiveStatus activeStatus = ExpertActiveStatus.ACTIVE;

    @Min(0)
    @Column
    private Integer mentoringPriceWon;

    @OneToMany(mappedBy = "expert", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<ExpertCareer> careers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "expert_tags", joinColumns = @JoinColumn(name = "expert_id"))
    @Column(name = "tag", length = 40, nullable = false)
    private Set<String> tags = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "expert_categories", joinColumns = @JoinColumn(name = "expert_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 40, nullable = false)
    private Set<TagCategory> categories = new LinkedHashSet<>();

    public static Expert createBackoffice(
            String name,
            String email,
            String oneLineIntroduction,
            Collection<String> tags,
            Collection<TagCategory> categories
    ) {
        Assert.hasText(name, "name must not be blank");
        Assert.hasText(email, "email must not be blank");

        Expert expert = new Expert();
        expert.name = name;
        expert.email = email;
        expert.oneLineIntroduction = oneLineIntroduction;
        expert.activeStatus = ExpertActiveStatus.INACTIVE;

        if (tags != null && !tags.isEmpty()) {
            expert.tags.clear();
            expert.tags.addAll(tags);
        }

        if (categories != null && !categories.isEmpty()) {
            expert.categories.clear();
            expert.categories.addAll(categories);
        }

        return expert;
    }

    public void updateActiveStatus(ExpertActiveStatus activeStatus) {
        Assert.notNull(activeStatus, "activeStatus must not be null");
        this.activeStatus = activeStatus;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        Assert.hasText(profileImageUrl, "profileImageUrl must not be blank");
        this.profileImageUrl = profileImageUrl;
    }

    public void updateBasicInfo(
            String name, String email, String oneLineIntroduction,
            String detailedIntroduction, Long workedPeriod, Integer mentoringPriceWon
    ) {
        Assert.hasText(name, "name must not be blank");
        Assert.hasText(email, "email must not be blank");
        this.name = name;
        this.email = email;
        this.oneLineIntroduction = oneLineIntroduction;
        this.detailedIntroduction = detailedIntroduction;
        this.workedPeriod = workedPeriod;
        this.mentoringPriceWon = mentoringPriceWon;
    }

    public void replaceTags(Collection<String> tags) {
        this.tags.clear();
        if (tags != null && !tags.isEmpty()) {
            this.tags.addAll(tags);
        }
    }

    public void replaceCategories(Collection<TagCategory> categories) {
        this.categories.clear();
        if (categories != null && !categories.isEmpty()) {
            this.categories.addAll(categories);
        }
    }

    public void syncCareers(List<ExpertCareerUpdate> updates) {
        List<ExpertCareerUpdate> careerUpdates = updates != null ? updates : List.of();

        validateCareerUpdates(careerUpdates);

        Map<Long, ExpertCareer> careerById = careers.stream()
                .filter(career -> career.getId() != null)
                .collect(Collectors.toMap(
                        ExpertCareer::getId,
                        Function.identity(),
                        (a, b) -> a
                ));

        Set<Long> requestedIds = careerUpdates.stream()
                .map(ExpertCareerUpdate::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        careers.removeIf(career ->
                career.getId() != null && !requestedIds.contains(career.getId())
        );

        for (ExpertCareerUpdate update : careerUpdates) {
            if (update.id() == null) {
                careers.add(ExpertCareer.of(
                        this,
                        update.orderIndex(),
                        update.careerTitle(),
                        update.careerExplanation(),
                        update.careerStartedAt(),
                        update.careerEndedAt()
                ));
                continue;
            }

            ExpertCareer career = careerById.get(update.id());
            if (career == null) {
                throw new ExpertException(ExpertErrorType.EXPERT_CAREER_INVALID);
            }

            career.update(
                    update.orderIndex(),
                    update.careerTitle(),
                    update.careerExplanation(),
                    update.careerStartedAt(),
                    update.careerEndedAt()
            );
        }
    }

    private void validateCareerUpdates(List<ExpertCareerUpdate> careerUpdates) {
        Set<Integer> orderIndexes = careerUpdates.stream()
                .map(ExpertCareerUpdate::orderIndex)
                .collect(Collectors.toSet());

        Set<Long> requestedIds = careerUpdates.stream()
                .map(ExpertCareerUpdate::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        long requestedIdCount = careerUpdates.stream()
                .map(ExpertCareerUpdate::id)
                .filter(Objects::nonNull)
                .count();

        boolean hasDuplicateOrderIndex = orderIndexes.size() != careerUpdates.size();
        boolean hasDuplicateIds = requestedIds.size() != requestedIdCount;
        boolean hasInvalidPeriod = careerUpdates.stream().anyMatch(update ->
                update.orderIndex() == null
                        || update.orderIndex() < 0
                        || update.careerStartedAt() == null
                        || update.careerEndedAt() == null
                        || update.careerStartedAt().isAfter(update.careerEndedAt())
        );

        if (hasDuplicateOrderIndex || hasDuplicateIds || hasInvalidPeriod) {
            throw new ExpertException(ExpertErrorType.EXPERT_CAREER_INVALID);
        }
    }
}
