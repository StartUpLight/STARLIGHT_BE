package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionName;

@Getter
@Entity
@NoArgsConstructor
public class GrowthTactic {
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_model_id", unique = true)
    private SubSection growthModel;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_funding_id", unique = true)
    private SubSection growthFunding;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_entry_id", unique = true)
    private SubSection growthEntry;

    public static GrowthTactic create() {
        GrowthTactic growthTactic = new GrowthTactic();
        growthTactic.initializeSubSections();
        return growthTactic;
    }

    private void initializeSubSections() {
        this.growthModel = SubSection.createEmptySubSection(SubSectionName.GROWTH_MODEL);
        this.growthFunding = SubSection.createEmptySubSection(SubSectionName.GROWTH_FUNDING);
        this.growthEntry = SubSection.createEmptySubSection(SubSectionName.GROWTH_ENTRY);
    }
}
