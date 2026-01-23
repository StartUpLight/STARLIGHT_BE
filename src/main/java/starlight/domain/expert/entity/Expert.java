package starlight.domain.expert.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.expert.enumerate.ExpertActiveStatus;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.shared.AbstractEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
}
